package eu.cepol.eventoperations.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
class SyntheticLoginIntegrationTest {
  @Autowired MockMvc mvc;
  @Test void fixtureAmCanLoginAndUseSession() throws Exception {
    var result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"am.alex\",\"password\":\"demo-password\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.username").value("am.alex")).andExpect(jsonPath("$.roles[0]").value("ROLE_AM")).andReturn();
    mvc.perform(get("/api/me").session((MockHttpSession) result.getRequest().getSession())).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("am.alex"));
  }
  @Test void badCredentialsAndAnonymousProfileAreDenied() throws Exception {
    mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"am.alex\",\"password\":\"wrong\"}")) .andExpect(status().isUnauthorized());
    mvc.perform(get("/api/me")).andExpect(status().isForbidden());
  }
  @Test void everyIdentifiedProfileCanAuthenticate() throws Exception {
    for (String username : new String[]{"am.alex", "po.petra", "ia.ines", "ao.aaron", "finance.fran", "provider.pavel", "cnu.clara", "cnu.niko", "attendee.aria", "admin.taylor"}) {
      mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"" + username + "\",\"password\":\"demo-password\"}"))
          .andExpect(status().isOk()).andExpect(jsonPath("$.username").value(username));
    }
  }
  @Test void logoutInvalidatesSession() throws Exception {
    var result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"am.alex\",\"password\":\"demo-password\"}"))
        .andExpect(status().isOk()).andReturn();
    var session = (MockHttpSession) result.getRequest().getSession();
    mvc.perform(post("/api/auth/logout").session(session)).andExpect(status().isOk());
    mvc.perform(get("/api/me").session(session)).andExpect(status().isForbidden());
  }
  @Test void activityListUsesTheAuthenticatedAssignmentScope() throws Exception {
    var result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"am.alex\",\"password\":\"demo-password\"}"))
        .andExpect(status().isOk()).andReturn();
    mvc.perform(get("/api/v1/activities").session((MockHttpSession) result.getRequest().getSession()))
        .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.courseReference == '2026-DEM-01')]").isNotEmpty());
  }

  @Test void teamMemberCanCreateActivityAndRecordNominationInvitations() throws Exception {
    var result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"ia.ines\",\"password\":\"demo-password\"}"))
        .andExpect(status().isOk()).andReturn();
    var session = (MockHttpSession) result.getRequest().getSession();
    var code = "2026-TEST-" + UUID.randomUUID().toString().substring(0, 8);
    var created = mvc.perform(post("/api/v1/activities").session(session).contentType(MediaType.APPLICATION_JSON)
            .content("{\"code\":\"" + code + "\",\"title\":\"Test course\",\"description\":\"Setup acceptance test\",\"countryCode\":\"HU\",\"venue\":\"Budapest\",\"timeZone\":\"Europe/Budapest\",\"startsOn\":\"2026-06-01\",\"endsOn\":\"2026-06-03\",\"expectedParticipants\":12,\"fundingRegime\":\"STANDARD\",\"invitationModality\":\"NOMINATION\",\"cplReference\":\"CPL-TEST\",\"cplByCostType\":{\"travel\":\"CPL-TRAVEL\"}}"))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.courseReference").value(code)).andReturn();
    var id = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.id");
    mvc.perform(post("/api/v1/activities/" + id + "/nomination-invitations").session(session).contentType(MediaType.APPLICATION_JSON)
            .content("{\"cnuUsernames\":[\"cnu.clara\",\"cnu.niko\"]}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.invitedCount").value(2));
  }

  @Test void setupValidatesModalityAndSupportsCurriculumAndCnuLookup() throws Exception {
    var login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"am.alex\",\"password\":\"demo-password\"}"))
        .andExpect(status().isOk()).andReturn();
    var session = (MockHttpSession) login.getRequest().getSession();
    mvc.perform(get("/api/v1/cnus").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$[0].username").value("cnu.clara"));
    var code = "2026-UPLOAD-" + UUID.randomUUID().toString().substring(0, 8);
    var created = mvc.perform(post("/api/v1/activities").session(session).contentType(MediaType.APPLICATION_JSON)
            .content("{\"code\":\"" + code + "\",\"title\":\"Upload test\",\"countryCode\":\"HU\",\"venue\":\"Budapest\",\"timeZone\":\"Europe/Budapest\",\"startsOn\":\"2026-06-01\",\"endsOn\":\"2026-06-03\",\"expectedParticipants\":1,\"fundingRegime\":\"STANDARD\",\"invitationModality\":\"NOMINATION\"}"))
        .andExpect(status().isCreated()).andReturn();
    var id = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.id");
    mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/activities/" + id + "/curriculum")
            .file(new MockMultipartFile("file", "curriculum.pdf", "application/pdf", "evidence".getBytes())).session(session))
        .andExpect(status().isOk()).andExpect(jsonPath("$.curriculaFileName").value("curriculum.pdf"));
    mvc.perform(post("/api/v1/activities").session(session).contentType(MediaType.APPLICATION_JSON)
            .content("{\"code\":\"BAD-MODALITY\",\"title\":\"Bad\",\"countryCode\":\"HU\",\"venue\":\"Budapest\",\"timeZone\":\"Europe/Budapest\",\"fundingRegime\":\"STANDARD\",\"invitationModality\":\"OPEN\"}"))
        .andExpect(status().isBadRequest());
    mvc.perform(post("/api/v1/activities/" + id + "/nomination-invitations").session(session).contentType(MediaType.APPLICATION_JSON).content("{\"cnuUsernames\":[]}"))
        .andExpect(status().isBadRequest());
  }

  @Test void nonTeamProfileCannotCreateActivity() throws Exception {
    var login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"cnu.clara\",\"password\":\"demo-password\"}"))
        .andExpect(status().isOk()).andReturn();
    mvc.perform(post("/api/v1/activities").session((MockHttpSession) login.getRequest().getSession())
            .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"FORBIDDEN\",\"title\":\"No\",\"description\":\"x\",\"countryCode\":\"HU\",\"venue\":\"Budapest\",\"timeZone\":\"Europe/Budapest\",\"startsOn\":\"2026-06-01\",\"endsOn\":\"2026-06-02\",\"expectedParticipants\":1,\"fundingRegime\":\"STANDARD\",\"invitationModality\":\"NOMINATION\"}"))
        .andExpect(status().isForbidden());
  }
}
