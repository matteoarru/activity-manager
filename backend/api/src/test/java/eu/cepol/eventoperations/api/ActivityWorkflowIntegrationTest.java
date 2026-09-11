package eu.cepol.eventoperations.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Exercises the production component wiring for the implemented activity workflow without mocks.
 */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:activity-workflow-tests;DB_CLOSE_DELAY=-1",
      "app.curricula.root=target/activity-workflow-test-curricula"
    })
@AutoConfigureMockMvc
class ActivityWorkflowIntegrationTest {
  private static final Path CURRICULUM_ROOT = Path.of("target/activity-workflow-test-curricula");

  @Autowired MockMvc mvc;
  @Autowired DataSource dataSource;

  @AfterEach
  void cleanTestArtefacts() throws Exception {
    var jdbc = new JdbcTemplate(dataSource);
    jdbc.update(
        "DELETE FROM activity_invitation WHERE activity_id IN "
            + "(SELECT id FROM activity WHERE course_reference LIKE 'INT-%')");
    jdbc.update("DELETE FROM activity WHERE course_reference LIKE 'INT-%'");
    if (Files.exists(CURRICULUM_ROOT)) {
      try (var paths = Files.walk(CURRICULUM_ROOT)) {
        for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
          Files.deleteIfExists(path);
        }
      }
    }
  }

  @Test
  void authenticatedActivityWorkflowPersistsAcrossHttpRepositoryAndStorageComponents()
      throws Exception {
    MockHttpSession session = loginAs("am.alex");
    String code = "INT-" + UUID.randomUUID().toString().substring(0, 8);

    var created =
        mvc.perform(
                post("/api/v1/activities")
                    .session(session)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"code\":\""
                            + code
                            + "\",\"title\":\"Integration activity\",\"description\":\"Created across components\",\"countryCode\":\"HU\",\"venue\":\"Budapest\",\"timeZone\":\"Europe/Budapest\",\"startsOn\":\"2027-01-10\",\"endsOn\":\"2027-01-12\",\"expectedParticipants\":12,\"fundingRegime\":\"STANDARD\",\"invitationModality\":\"NOMINATION\",\"cplReference\":\"CPL-INT\",\"cplByCostType\":{\"travel\":\"CPL-TRAVEL\"}}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.courseReference").value(code))
            .andReturn();
    String id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

    mvc.perform(
            put("/api/v1/activities/" + id)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"title\":\"Updated integration activity\",\"description\":\"Updated across components\",\"venue\":\"The Hague\",\"startsOn\":\"2027-01-11\",\"endsOn\":\"2027-01-13\",\"expectedParticipants\":14}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Updated integration activity"));

    mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .multipart("/api/v1/activities/" + id + "/curriculum")
                .file(
                    new MockMultipartFile(
                        "file", "integration-curriculum.pdf", "application/pdf", "evidence".getBytes()))
                .session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.curriculaFileName").value("integration-curriculum.pdf"));

    mvc.perform(
            post("/api/v1/activities/" + id + "/nomination-invitations")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cnuUsernames\":[\"cnu.clara\",\"cnu.niko\"]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.invitedCount").value(2));

    mvc.perform(get("/api/v1/activities").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.courseReference == '" + code + "')].title").value("Updated integration activity"));

    var jdbc = new JdbcTemplate(dataSource);
    String objectKey =
        jdbc.queryForObject(
            "SELECT curricula_object_key FROM activity WHERE id = ?", String.class, id);
    Integer invitations =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM activity_invitation WHERE activity_id = ?", Integer.class, id);
    org.junit.jupiter.api.Assertions.assertAll(
        () -> org.junit.jupiter.api.Assertions.assertTrue(Files.exists(CURRICULUM_ROOT.resolve(objectKey))),
        () -> org.junit.jupiter.api.Assertions.assertEquals(2, invitations));
  }

  private MockHttpSession loginAs(String username) throws Exception {
    var result =
        mvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"" + username + "\",\"password\":\"demo-password\"}"))
            .andExpect(status().isOk())
            .andReturn();
    return (MockHttpSession) result.getRequest().getSession();
  }
}
