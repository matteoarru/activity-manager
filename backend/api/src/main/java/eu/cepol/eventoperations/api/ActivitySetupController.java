package eu.cepol.eventoperations.api;

import eu.cepol.eventoperations.infrastructure.activity.ActivityRecord;
import eu.cepol.eventoperations.infrastructure.activity.ActivityRepository;
import eu.cepol.eventoperations.infrastructure.activity.CurriculumStorage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1")
class ActivitySetupController {
  private static final String NOMINATION = "NOMINATION";
  private static final List<String> TEAM_ROLES =
      List.of("ROLE_AM", "ROLE_PO", "ROLE_IA", "ROLE_AO");

  private final ActivityRepository activities;
  private final CurriculumStorage curricula;
  private final ObjectMapper json;

  ActivitySetupController(ActivityRepository activities, CurriculumStorage curricula, ObjectMapper json) {
    this.activities = activities;
    this.curricula = curricula;
    this.json = json;
  }

  @PostMapping("/activities")
  @ResponseStatus(HttpStatus.CREATED)
  ActivityRecord create(@Valid @RequestBody CreateActivity request, Authentication actor) {
    requireTeam(actor);
    List<String> managerUsernames = managersIncludingCreator(actor.getName(), request.managerUsernames());
    if (!NOMINATION.equals(request.invitationModality())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only NOMINATION invitation modality is currently supported");
    }
    final String cpl;
    try {
      cpl = json.writeValueAsString(request.cplByCostType());
    } catch (RuntimeException e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "CPL allocation is not valid JSON", e);
    }
    return activities.create(
        request.arn(),
        request.title(),
        request.description(),
        request.countryCode(),
        request.venue(),
        request.timeZone(),
        request.startsOn(),
        request.endsOn(),
        request.expectedParticipants(),
        request.fundingRegime(),
        primaryManager(managerUsernames),
        additionalManagers(managerUsernames),
        request.cplReference(),
        cpl,
        request.invitationModality(),
        request.nominationDeadline());
  }

  @PutMapping("/activities/{id}")
  ActivityRecord update(
      @PathVariable("id") String id,
      @Valid @RequestBody UpdateActivity request,
      Authentication actor) {
    requireManagerOf(id, actor);
    return activities.update(
        id,
        request.arn(),
        request.title(),
        request.description(),
        request.venue(),
        request.startsOn(),
        request.endsOn(),
        request.expectedParticipants(),
        primaryManager(request.managerUsernames()),
        additionalManagers(request.managerUsernames()));
  }

  @PostMapping(value = "/activities/{id}/curriculum", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ActivityRecord uploadCurriculum(
      @PathVariable("id") String id,
      @RequestPart("file") MultipartFile file,
      Authentication actor) {
    requireManagerOf(id, actor);
    try {
      var stored = curricula.store(file.getBytes());
      activities.attachCurriculum(
          id, safeName(file.getOriginalFilename()), stored.objectKey(), stored.sha256());
      return activities.findAll().stream()
          .filter(a -> a.id().equals(id))
          .findFirst()
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Curriculum could not be stored", e);
    }
  }

  @PostMapping("/activities/{id}/nomination-invitations")
  InvitationResult inviteCnus(
      @PathVariable("id") String id,
      @RequestBody InvitationRequest request,
      Authentication actor) {
    requireManagerOf(id, actor);
    if (request.cnuUsernames() == null || request.cnuUsernames().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select at least one CNU");
    }
    int sent = activities.invite(id, request.cnuUsernames().stream().distinct().toList(), actor.getName());
    activities.markInvited(id, request.nominationDeadline());
    return new InvitationResult(activities.findById(id), sent);
  }

  @PostMapping("/activities/{id}/nominations")
  @ResponseStatus(HttpStatus.CREATED)
  Map<String, String> nominate(@PathVariable("id") String id, @Valid @RequestBody NominationRequest request, Authentication actor) {
    if (!actor.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CNU"))) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only CNUs may submit nominations");
    }
    ActivityRecord activity = activities.findById(id);
    if (!"INVITED".equals(activity.status())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Nominations are not open for this activity");
    }
    activities.addNomination(id, actor.getName(), request.nomineeName());
    return Map.of("status", "submitted");
  }

  @GetMapping("/activities/{id}/nominations")
  List<Map<String, Object>> nominations(@PathVariable("id") String id, Authentication actor) {
    requireManagerOf(id, actor);
    return activities.nominations(id);
  }

  @PostMapping("/activities/{id}/nominations/select")
  ActivityRecord selectNominations(@PathVariable("id") String id, @RequestBody SelectionRequest request, Authentication actor) {
    requireManagerOf(id, actor);
    if (!"AWAITING_SELECTION".equals(activities.findById(id).status())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Nominations may be selected after the nomination deadline");
    }
    if (request.nominationIds() == null || request.nominationIds().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select at least one nomination");
    }
    activities.selectNominations(id, request.nominationIds().stream().distinct().toList());
    return activities.findById(id);
  }

  @PostMapping("/activities/{id}/cost-lines")
  Map<String, String> addCostLine(@PathVariable("id") String id, @Valid @RequestBody CostLineRequest request, Authentication actor) {
    requireManagerOf(id, actor);
    return Map.of("id", activities.addCostLine(id, request.description()));
  }

  @PostMapping("/activities/{id}/cost-lines/{costLineId}/paid")
  void markCostLinePaid(@PathVariable("id") String id, @PathVariable("costLineId") String costLineId, Authentication actor) {
    requireManagerOf(id, actor);
    activities.markCostLinePaid(id, costLineId);
  }

  @PostMapping("/activities/{id}/close")
  ActivityRecord close(@PathVariable("id") String id, Authentication actor) {
    requireManagerOf(id, actor);
    ActivityRecord activity = activities.findById(id);
    if (!"CONCLUDED".equals(activity.status()) || !activities.allCostLinesPaid(id)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Concluded activity must have all cost lines paid before closing");
    }
    activities.setStatus(id, "CLOSED");
    return activities.findById(id);
  }

  @PostMapping("/activities/{id}/cancel")
  ActivityRecord cancel(@PathVariable("id") String id, Authentication actor) {
    requireManagerOf(id, actor);
    activities.setStatus(id, "CANCELLED");
    return activities.findById(id);
  }

  @GetMapping("/cnus")
  List<Map<String, Object>> cnus(Authentication actor) {
    requireTeam(actor);
    return activities.findCnus();
  }

  @GetMapping("/activity-managers")
  List<Map<String, String>> activityManagers(Authentication actor) {
    requireTeam(actor);
    return activities.findActivityManagers();
  }

  private String primaryManager(List<String> managerUsernames) {
    validateManagers(managerUsernames);
    return managerUsernames.getFirst();
  }

  private List<String> managersIncludingCreator(String creator, List<String> requestedManagers) {
    return Stream.concat(Stream.of(creator), requestedManagers.stream()).distinct().toList();
  }

  private String additionalManagers(List<String> managerUsernames) {
    validateManagers(managerUsernames);
    return String.join(",", managerUsernames.stream().skip(1).toList());
  }

  private void validateManagers(List<String> managerUsernames) {
    var eligible = activities.findActivityManagers().stream().map(manager -> manager.get("username")).toList();
    if (managerUsernames.stream().distinct().count() != managerUsernames.size()
        || !eligible.containsAll(managerUsernames)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select one or more eligible activity managers");
    }
  }

  private void requireTeam(Authentication actor) {
    boolean teamMember = actor.getAuthorities().stream()
        .anyMatch(authority -> TEAM_ROLES.contains(authority.getAuthority()));
    if (!teamMember) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Only AM, PO, IA or AO may manage an activity");
    }
  }

  private void requireManagerOf(String id, Authentication actor) {
    requireTeam(actor);
    boolean assigned = isAuthorisingOfficer(actor)
        || activities.findVisibleTo(actor.getName()).stream().anyMatch(a -> a.id().equals(id));
    if (!assigned) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Activity is outside your assignment scope");
    }
  }

  private boolean isAuthorisingOfficer(Authentication actor) {
    return actor.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals("ROLE_AO"));
  }

  private static String safeName(String original) {
    if (original == null || original.isBlank()) {
      return "curriculum-upload";
    }
    return original.replaceAll("[^A-Za-z0-9._-]", "_");
  }
  record InvitationRequest(List<String> cnuUsernames, @NotNull java.time.LocalDate nominationDeadline) {}
  record InvitationResult(ActivityRecord activity, int invitedCount) {}
  record NominationRequest(@NotBlank String nomineeName) {}
  record SelectionRequest(List<String> nominationIds) {}
  record CostLineRequest(@NotBlank String description) {}

  record UpdateActivity(
      @NotBlank String arn,
      @NotBlank String title,
      String description,
      @NotBlank String venue,
      @NotNull java.time.LocalDate startsOn,
      @NotNull java.time.LocalDate endsOn,
      @Min(0) int expectedParticipants,
      @NotEmpty List<String> managerUsernames) {
    UpdateActivity {
      description = description == null ? "" : description;
    }
  }

  record CreateActivity(
      @NotBlank String arn,
      @NotBlank String title,
      String description,
      @NotBlank String countryCode,
      @NotBlank String venue,
      @NotBlank String timeZone,
      java.time.LocalDate startsOn,
      java.time.LocalDate endsOn,
      @Min(0) int expectedParticipants,
      @NotBlank String fundingRegime,
      @NotEmpty List<String> managerUsernames,
      String cplReference,
      Map<String, String> cplByCostType,
      String invitationModality,
      @NotNull java.time.LocalDate nominationDeadline) {
    CreateActivity {
      description = description == null ? "" : description;
      managerUsernames = managerUsernames == null ? List.of() : managerUsernames;
      cplByCostType = cplByCostType == null ? Map.of() : cplByCostType;
      invitationModality = invitationModality == null ? NOMINATION : invitationModality;
    }
  }
}
