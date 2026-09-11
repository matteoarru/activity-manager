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
          + "curricula_file_name, curricula_object_key, curricula_sha256";
  private static final String ACTIVITY_SELECT = "SELECT " + ACTIVITY_COLUMNS + " FROM activity ";
  private static final String ACTIVITY_SCOPE_ORDER = " ORDER BY starts_on";

  private final JdbcTemplate jdbc;

  public ActivityRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<ActivityRecord> findVisibleTo(String username) {
    return jdbc.query(
        ACTIVITY_SELECT
            + "WHERE am_username = ? OR (',' || support_usernames || ',') LIKE ?"
            + ACTIVITY_SCOPE_ORDER,
        this::mapActivity,
        username,
        "%," + username + ",%");
  }

  public List<ActivityRecord> findAll() {
    return jdbc.query(ACTIVITY_SELECT + ACTIVITY_SCOPE_ORDER, this::mapActivity);
  }

  public ActivityRecord findById(String id) {
    return jdbc.queryForObject(ACTIVITY_SELECT + "WHERE id = ?", this::mapActivity, id);
  }

  public ActivityRecord create(
      String code,
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
      String invitationModality) {
    String id = UUID.randomUUID().toString();
    jdbc.update(
        "INSERT INTO activity (id, course_reference, title, description, country_code, venue, "
            + "time_zone, starts_on, ends_on, status, funding_regime, am_username, "
            + "support_usernames, expected_participants, invitation_modality, cpl_reference, "
            + "cpl_by_cost_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        id,
        code,
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
        cplByCostType);
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
      String title,
      String description,
      String venue,
      LocalDate startsOn,
      LocalDate endsOn,
      int expectedParticipants) {
    jdbc.update(
        "UPDATE activity SET title = ?, description = ?, venue = ?, starts_on = ?, ends_on = ?, "
            + "expected_participants = ? WHERE id = ?",
        title,
        description,
        venue,
        startsOn,
        endsOn,
        expectedParticipants,
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
        rs.getString("curricula_sha256"));
  }

  private static Map<String, Object> mapCnu(ResultSet rs) throws SQLException {
    return Map.of(
        "username", rs.getString("username"),
        "organisation", rs.getString("organisation"),
        "countryCode", rs.getString("country_code"));
  }
}
