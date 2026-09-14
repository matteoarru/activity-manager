package eu.cepol.eventoperations.infrastructure.activity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ActivityRepository {
  private static final String ACTIVITY_COLUMNS =
      "id, course_reference, title, description, country_code, venue, time_zone, "
          + "starts_on, ends_on, status, funding_regime, am_username, support_usernames, "
          + "expected_participants, invitation_modality, cpl_reference, cpl_by_cost_type, "
          + "curricula_file_name, curricula_object_key, curricula_sha256, nomination_deadline";
  private static final String ACTIVITY_SELECT = "SELECT " + ACTIVITY_COLUMNS + " FROM activity ";
  private static final String ACTIVITY_SCOPE_ORDER = " ORDER BY starts_on";

  private final JdbcTemplate jdbc;

  public ActivityRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<ActivityRecord> findVisibleTo(String username) {
    refreshTimeTransitions();
    return jdbc.query(
        ACTIVITY_SELECT
            + "WHERE am_username = ? OR (',' || support_usernames || ',') LIKE ?"
            + ACTIVITY_SCOPE_ORDER,
        this::mapActivity,
        username,
        "%," + username + ",%");
  }

  public List<ActivityRecord> findAll() {
    refreshTimeTransitions();
    return jdbc.query(ACTIVITY_SELECT + ACTIVITY_SCOPE_ORDER, this::mapActivity);
  }

  public ActivityRecord findById(String id) {
    refreshTimeTransitions();
    return jdbc.queryForObject(ACTIVITY_SELECT + "WHERE id = ?", this::mapActivity, id);
  }

  public ActivityRecord create(
      String arn,
      String title,
      String description,
      String countryCode,
      String venue,
      String timeZone,
      LocalDate startsOn,
      LocalDate endsOn,
      int expectedParticipants,
      String fundingRegime,
      String amUsername,
      String supportUsernames,
      String cplReference,
      String cplByCostType,
      String invitationModality,
      LocalDate nominationDeadline) {
    String id = UUID.randomUUID().toString();
    jdbc.update(
        "INSERT INTO activity (id, activity_code, course_reference, title, description, country_code, venue, "
            + "time_zone, starts_on, ends_on, status, funding_regime, am_username, "
            + "support_usernames, expected_participants, invitation_modality, cpl_reference, "
            + "cpl_by_cost_type, nomination_deadline) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        id,
        "ACT-" + UUID.randomUUID(),
        arn,
        title,
        description,
        countryCode,
        venue,
        timeZone,
        startsOn,
        endsOn,
        "DRAFT",
        fundingRegime,
        amUsername,
        supportUsernames,
        expectedParticipants,
        invitationModality,
        cplReference,
        cplByCostType,
        nominationDeadline);
    return jdbc.queryForObject(ACTIVITY_SELECT + "WHERE id = ?", this::mapActivity, id);
  }

  public void attachCurriculum(String id, String fileName, String objectKey, String sha256) {
    jdbc.update(
        "UPDATE activity SET curricula_file_name = ?, curricula_object_key = ?, "
            + "curricula_sha256 = ? WHERE id = ?",
        fileName,
        objectKey,
        sha256,
        id);
  }

  public ActivityRecord update(
      String id,
      String arn,
      String title,
      String description,
      String venue,
      LocalDate startsOn,
      LocalDate endsOn,
      int expectedParticipants,
      String amUsername,
      String supportUsernames) {
    jdbc.update(
        "UPDATE activity SET course_reference = ?, title = ?, description = ?, venue = ?, starts_on = ?, ends_on = ?, "
            + "expected_participants = ?, am_username = ?, support_usernames = ? WHERE id = ?",
        arn,
        title,
        description,
        venue,
        startsOn,
        endsOn,
        expectedParticipants,
        amUsername,
        supportUsernames,
        id);
    return findById(id);
  }

  public int invite(String activityId, List<String> cnuUsernames, String sentBy) {
    return cnuUsernames.stream()
        .mapToInt(
            cnu ->
                jdbc.update(
                    "MERGE INTO activity_invitation "
                        + "(activity_id, cnu_username, modality, sent_at, sent_by) "
                        + "KEY (activity_id, cnu_username) VALUES (?, ?, ?, ?, ?)",
                    activityId,
                    cnu,
                    "NOMINATION",
                    Instant.now(),
                    sentBy))
        .sum();
  }

  public List<Map<String, Object>> findCnus() {
    return jdbc.query(
        "SELECT username, organisation, country_code FROM cnu "
            + "WHERE active = TRUE ORDER BY organisation",
        (rs, rowNumber) -> mapCnu(rs));
  }

  public List<Map<String, String>> findActivityManagers() {
    return List.of(
        Map.of("username", "am.alex", "role", "AM"),
        Map.of("username", "po.petra", "role", "PO"),
        Map.of("username", "ia.ines", "role", "IA"),
        Map.of("username", "ao.aaron", "role", "AO"));
  }

  private ActivityRecord mapActivity(ResultSet rs, int rowNumber) throws SQLException {
    return new ActivityRecord(
        rs.getString("id"),
        rs.getString("course_reference"),
        rs.getString("title"),
        rs.getString("description"),
        rs.getString("country_code"),
        rs.getString("venue"),
        rs.getString("time_zone"),
        rs.getObject("starts_on", LocalDate.class),
        rs.getObject("ends_on", LocalDate.class),
        rs.getString("status"),
        rs.getString("funding_regime"),
        rs.getString("am_username"),
        rs.getString("support_usernames"),
        rs.getInt("expected_participants"),
        rs.getString("invitation_modality"),
        rs.getString("cpl_reference"),
        rs.getString("cpl_by_cost_type"),
        rs.getString("curricula_file_name"),
        rs.getString("curricula_object_key"),
        rs.getString("curricula_sha256"),
        rs.getObject("nomination_deadline", LocalDate.class));
  }

  public void markInvited(String id, LocalDate nominationDeadline) {
    jdbc.update("UPDATE activity SET status = ?, nomination_deadline = ? WHERE id = ?", "INVITED", nominationDeadline, id);
  }

  public void addNomination(String activityId, String cnuUsername, String nomineeName) {
    jdbc.update("INSERT INTO activity_nomination (id, activity_id, cnu_username, nominee_name) VALUES (?, ?, ?, ?)", UUID.randomUUID().toString(), activityId, cnuUsername, nomineeName);
  }

  public List<Map<String, Object>> nominations(String activityId) {
    return jdbc.query("SELECT id, cnu_username, nominee_name, selected FROM activity_nomination WHERE activity_id = ? ORDER BY created_at", (rs, row) -> Map.of("id", rs.getString("id"), "cnuUsername", rs.getString("cnu_username"), "nomineeName", rs.getString("nominee_name"), "selected", rs.getBoolean("selected")), activityId);
  }

  public void selectNominations(String activityId, List<String> nominationIds) {
    jdbc.update("UPDATE activity_nomination SET selected = FALSE WHERE activity_id = ?", activityId);
    for (String nominationId : nominationIds) {
      jdbc.update("UPDATE activity_nomination SET selected = TRUE WHERE id = ? AND activity_id = ?", nominationId, activityId);
    }
    jdbc.update("UPDATE activity SET status = ? WHERE id = ?", "ACCEPTED", activityId);
  }

  public String addCostLine(String activityId, String description) {
    String id = UUID.randomUUID().toString();
    jdbc.update("INSERT INTO activity_cost_line (id, activity_id, description) VALUES (?, ?, ?)", id, activityId, description);
    return id;
  }

  public void markCostLinePaid(String activityId, String costLineId) {
    jdbc.update("UPDATE activity_cost_line SET paid = TRUE WHERE id = ? AND activity_id = ?", costLineId, activityId);
  }

  public boolean allCostLinesPaid(String activityId) {
    Integer outstanding = jdbc.queryForObject("SELECT COUNT(*) FROM activity_cost_line WHERE activity_id = ? AND paid = FALSE", Integer.class, activityId);
    Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM activity_cost_line WHERE activity_id = ?", Integer.class, activityId);
    return total != null && total > 0 && outstanding != null && outstanding == 0;
  }

  public void setStatus(String activityId, String status) {
    jdbc.update("UPDATE activity SET status = ? WHERE id = ?", status, activityId);
  }

  private void refreshTimeTransitions() {
    jdbc.update("UPDATE activity SET status = 'AWAITING_SELECTION' WHERE status = 'INVITED' AND nomination_deadline < CURRENT_DATE");
    jdbc.update("UPDATE activity SET status = 'CONCLUDED' WHERE status = 'ACCEPTED' AND ends_on < CURRENT_DATE");
  }

  private static Map<String, Object> mapCnu(ResultSet rs) throws SQLException {
    return Map.of(
        "username", rs.getString("username"),
        "organisation", rs.getString("organisation"),
        "countryCode", rs.getString("country_code"));
  }
}
