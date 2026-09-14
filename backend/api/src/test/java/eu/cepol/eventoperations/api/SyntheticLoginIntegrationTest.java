package eu.cepol.eventoperations.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:api-tests;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class SyntheticLoginIntegrationTest {
  private static final String DEMO_PASSWORD = "demo-password";

  @Autowired MockMvc mvc;
  @Autowired DataSource dataSource;

  @Test
  void fixtureAmCanLoginAndUseSession() throws Exception {
    MockHttpSession session = loginAs("am.alex");

    mvc.perform(get("/api/me").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("am.alex"))
        .andExpect(jsonPath("$.roles[0]").value("ROLE_AM"));
  }

  @Test
  void badCredentialsAndAnonymousProfileAreDenied() throws Exception {
    mvc.perform(loginRequest("am.alex", "wrong")).andExpect(status().isUnauthorized());

    mvc.perform(get("/api/me")).andExpect(status().isForbidden());
  }

  @Test
  void everyIdentifiedProfileCanAuthenticate() throws Exception {
    for (String username :
        new String[] {
          "am.alex",
          "po.petra",
          "ia.ines",
          "ao.aaron",
          "finance.fran",
          "provider.pavel",
          "cnu.clara",
          "cnu.niko",
          "attendee.aria",
          "admin.taylor"
        }) {
      loginAs(username);
    }
  }

  @Test
  void logoutInvalidatesSession() throws Exception {
    MockHttpSession session = loginAs("am.alex");

    mvc.perform(post("/api/auth/logout").session(session)).andExpect(status().isOk());
    mvc.perform(get("/api/me").session(session)).andExpect(status().isForbidden());
  }

  @Test
  void activityListUsesTheAuthenticatedAssignmentScope() throws Exception {
    MockHttpSession session = loginAs("am.alex");

    mvc.perform(get("/api/v1/activities").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.courseReference == '2026-DEM-01')]").isNotEmpty());
  }

  @Test
  void activityListIsAssignedOnlyByDefaultAndCanBeExplicitlyWidened() throws Exception {
    MockHttpSession amSession = loginAs("am.alex");
    String arn = uniqueCode("ASSIGNED-TO-PO");
    createActivity(loginAs("po.petra"), arn, "Assigned to PO", "po.petra");

    mvc.perform(get("/api/v1/activities").session(amSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.courseReference == '" + arn + "')]").isEmpty());
    mvc.perform(get("/api/v1/activities?showAll=true").session(amSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.courseReference == '" + arn + "')]").isNotEmpty());
  }

  @Test
  void teamMemberCanCreateActivityAndRecordNominationInvitations() throws Exception {
    MockHttpSession session = loginAs("ia.ines");
    String id = createActivity(session, uniqueCode("TEST"), "Test course", "ia.ines", "po.petra");

    mvc.perform(
            post("/api/v1/activities/" + id + "/nomination-invitations")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cnuUsernames\":[\"cnu.clara\",\"cnu.niko\"],\"nominationDeadline\":\"2027-01-01\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.invitedCount").value(2));
  }

  @Test
  void creatorIsAlwaysAssignedEvenWhenOnlyAnotherManagerIsRequested() throws Exception {
    MockHttpSession session = loginAs("ia.ines");
    String arn = uniqueCode("CREATOR");

    mvc.perform(
            post("/api/v1/activities")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validActivityRequest(arn, "Creator assignment", "NOMINATION", "po.petra")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.amUsername").value("ia.ines"))
        .andExpect(jsonPath("$.supportUsernames").value("po.petra"));

    mvc.perform(get("/api/v1/activities").session(session))
        .andExpect(jsonPath("$[?(@.courseReference == '" + arn + "')]").isNotEmpty());
  }

  @Test
  void lifecycleMovesFromInvitationThroughSelectionConclusionClosureAndCancellation() throws Exception {
    MockHttpSession manager = loginAs("am.alex");
    String id = createActivity(manager, uniqueCode("LIFECYCLE"), "Lifecycle activity", "am.alex");
    new JdbcTemplate(dataSource).update(
        "UPDATE activity SET starts_on = DATE '2099-01-10', ends_on = DATE '2099-01-12' WHERE id = ?", id);
    mvc.perform(post("/api/v1/activities/" + id + "/nomination-invitations").session(manager)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"cnuUsernames\":[\"cnu.clara\"],\"nominationDeadline\":\"2099-01-01\"}"))
        .andExpect(status().isOk());
    mvc.perform(get("/api/v1/activities").session(manager))
        .andExpect(jsonPath("$[?(@.id == '" + id + "')].status").value("INVITED"));

    MockHttpSession cnu = loginAs("cnu.clara");
    mvc.perform(post("/api/v1/activities/" + id + "/nominations").session(cnu)
            .contentType(MediaType.APPLICATION_JSON).content("{\"nomineeName\":\"First nominee\"}"))
        .andExpect(status().isCreated());
    mvc.perform(post("/api/v1/activities/" + id + "/nominations").session(cnu)
            .contentType(MediaType.APPLICATION_JSON).content("{\"nomineeName\":\"Second nominee\"}"))
        .andExpect(status().isCreated());
    var nominations = mvc.perform(get("/api/v1/activities/" + id + "/nominations").session(manager))
        .andExpect(status().isOk()).andReturn();
    String nominationId = JsonPath.read(nominations.getResponse().getContentAsString(), "$[0].id");
    new JdbcTemplate(dataSource).update("UPDATE activity SET nomination_deadline = DATE '2020-01-01' WHERE id = ?", id);
    mvc.perform(post("/api/v1/activities/" + id + "/nominations/select").session(manager)
            .contentType(MediaType.APPLICATION_JSON).content("{\"nominationIds\":[\"" + nominationId + "\"]}"))
        .andExpect(jsonPath("$.status").value("ACCEPTED"));

    new JdbcTemplate(dataSource).update(
        "UPDATE activity SET starts_on = DATE '2019-01-01', ends_on = DATE '2020-01-01' WHERE id = ?", id);
    mvc.perform(get("/api/v1/activities").session(manager))
        .andExpect(jsonPath("$[?(@.id == '" + id + "')].status").value("CONCLUDED"));
    String costLineId = JsonPath.read(mvc.perform(post("/api/v1/activities/" + id + "/cost-lines").session(manager)
            .contentType(MediaType.APPLICATION_JSON).content("{\"description\":\"Hotel\"}"))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), "$.id");
    mvc.perform(post("/api/v1/activities/" + id + "/cost-lines/" + costLineId + "/paid").session(manager))
        .andExpect(status().isOk());
    mvc.perform(post("/api/v1/activities/" + id + "/close").session(manager))
        .andExpect(jsonPath("$.status").value("CLOSED"));
    mvc.perform(post("/api/v1/activities/" + id + "/cancel").session(manager))
        .andExpect(jsonPath("$.status").value("CANCELLED"));
  }

  @Test
  void setupValidatesModalityAndSupportsCurriculumAndCnuLookup() throws Exception {
    MockHttpSession session = loginAs("am.alex");
    mvc.perform(get("/api/v1/cnus").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].username").value("cnu.clara"));
    String id = createActivity(session, uniqueCode("UPLOAD"), "Upload test", "am.alex");

    mvc.perform(
            multipart("/api/v1/activities/" + id + "/curriculum")
                .file(
                    new MockMultipartFile(
                        "file", "curriculum.pdf", "application/pdf", "evidence".getBytes()))
                .session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.curriculaFileName").value("curriculum.pdf"));
    mvc.perform(
            post("/api/v1/activities")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validActivityRequest("BAD-MODALITY", "Bad", "OPEN")))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/v1/activities/" + id + "/nomination-invitations")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cnuUsernames\":[],\"nominationDeadline\":\"2027-01-01\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void nonTeamProfileCannotCreateActivity() throws Exception {
    MockHttpSession session = loginAs("cnu.clara");

    mvc.perform(
            post("/api/v1/activities")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validActivityRequest("FORBIDDEN", "No", "NOMINATION")))
        .andExpect(status().isForbidden());
  }

  @Test
  void authorisingOfficerCanCreateAndEditAnyActivityButCnuCannotEdit() throws Exception {
    MockHttpSession aoSession = loginAs("ao.aaron");
    String id = createActivity(aoSession, uniqueCode("AO"), "AO course", "ao.aaron");

    mvc.perform(updateRequest(id, aoSession, "Edited by AO"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Edited by AO"));

    mvc.perform(updateRequest(id, loginAs("cnu.clara"), "Unauthorised"))
        .andExpect(status().isForbidden());
  }

  private MockHttpSession loginAs(String username) throws Exception {
    var result =
        mvc.perform(loginRequest(username, DEMO_PASSWORD))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value(username))
            .andReturn();
    return (MockHttpSession) result.getRequest().getSession();
  }

  private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder loginRequest(
      String username, String password) {
    return post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}");
  }

  private String createActivity(MockHttpSession session, String code, String title, String... managers) throws Exception {
    var result =
        mvc.perform(
                post("/api/v1/activities")
                    .session(session)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validActivityRequest(code, title, "NOMINATION", managers)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.courseReference").value(code))
            .andReturn();
    return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
  }

  private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder updateRequest(
      String id, MockHttpSession session, String title) {
    return put("/api/v1/activities/" + id)
        .session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            "{\"title\":\""
                + title
                + "\",\"description\":\"Updated\",\"venue\":\"The Hague\",\"startsOn\":\"2026-06-02\",\"endsOn\":\"2026-06-04\",\"expectedParticipants\":2,\"arn\":\"UPDATED-ARN\",\"managerUsernames\":[\"ao.aaron\"]}");
  }

  private static String uniqueCode(String prefix) {
    return "2026-" + prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
  }

  private static String validActivityRequest(String code, String title, String invitationModality, String... managers) {
    String managerJson = String.join("\",\"", managers.length == 0 ? new String[] {"am.alex"} : managers);
    return "{\"arn\":\""
        + code
        + "\",\"managerUsernames\":[\"" + managerJson + "\"],\"title\":\""
        + title
        + "\",\"description\":\"Setup acceptance test\",\"countryCode\":\"HU\",\"venue\":\"Budapest\",\"timeZone\":\"Europe/Budapest\",\"startsOn\":\"2026-06-01\",\"endsOn\":\"2026-06-03\",\"nominationDeadline\":\"2026-05-30\",\"expectedParticipants\":12,\"fundingRegime\":\"STANDARD\",\"invitationModality\":\""
        + invitationModality
        + "\",\"cplReference\":\"CPL-TEST\",\"cplByCostType\":{\"travel\":\"CPL-TRAVEL\"}}";
  }
}
