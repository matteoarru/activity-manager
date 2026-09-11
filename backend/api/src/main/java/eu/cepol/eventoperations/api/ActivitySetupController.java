package eu.cepol.eventoperations.api;

import eu.cepol.eventoperations.infrastructure.activity.ActivityRecord;
import eu.cepol.eventoperations.infrastructure.activity.ActivityRepository;
import eu.cepol.eventoperations.infrastructure.activity.CurriculumStorage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.Map;
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
        request.code(),
        request.title(),
        request.description(),
        request.countryCode(),
        request.venue(),
        request.timeZone(),
        request.startsOn(),
        request.endsOn(),
        request.expectedParticipants(),
        request.fundingRegime(),
        actor.getName(),
        String.join(",", request.supportUsernames()),
        request.cplReference(),
        cpl,
        request.invitationModality());
  }

  @PutMapping("/activities/{id}")
  ActivityRecord update(
      @PathVariable("id") String id,
      @Valid @RequestBody UpdateActivity request,
      Authentication actor) {
    requireManagerOf(id, actor);
    return activities.update(
        id,
        request.title(),
        request.description(),
        request.venue(),
        request.startsOn(),
        request.endsOn(),
        request.expectedParticipants());
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
  Map<String, Object> inviteCnus(
      @PathVariable("id") String id,
      @RequestBody InvitationRequest request,
      Authentication actor) {
    requireManagerOf(id, actor);
    if (request.cnuUsernames() == null || request.cnuUsernames().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select at least one CNU");
    }
    int sent = activities.invite(id, request.cnuUsernames().stream().distinct().toList(), actor.getName());
    return Map.of("activityId", id, "modality", NOMINATION, "invitedCount", sent);
  }

  @GetMapping("/cnus")
  List<Map<String, Object>> cnus(Authentication actor) {
    requireTeam(actor);
    return activities.findCnus();
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
  record InvitationRequest(List<String> cnuUsernames) {}

  record UpdateActivity(
      @NotBlank String title,
      String description,
      @NotBlank String venue,
      @NotNull java.time.LocalDate startsOn,
      @NotNull java.time.LocalDate endsOn,
      @Min(0) int expectedParticipants) {
    UpdateActivity {
      description = description == null ? "" : description;
    }
  }

  record CreateActivity(
      @NotBlank String code,
      @NotBlank String title,
      String description,
      @NotBlank String countryCode,
      @NotBlank String venue,
      @NotBlank String timeZone,
      java.time.LocalDate startsOn,
      java.time.LocalDate endsOn,
      @Min(0) int expectedParticipants,
      @NotBlank String fundingRegime,
      List<String> supportUsernames,
      String cplReference,
      Map<String, String> cplByCostType,
      String invitationModality) {
    CreateActivity {
      description = description == null ? "" : description;
      supportUsernames = supportUsernames == null ? List.of() : supportUsernames;
      cplByCostType = cplByCostType == null ? Map.of() : cplByCostType;
      invitationModality = invitationModality == null ? NOMINATION : invitationModality;
    }
  }
}
