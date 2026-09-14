package eu.cepol.eventoperations.infrastructure.activity;

import java.time.LocalDate;

public record ActivityRecord(String id, String courseReference, String title, String description, String countryCode,
    String venue, String timeZone, LocalDate startsOn, LocalDate endsOn, String status, String fundingRegime,
    String amUsername, String supportUsernames, int expectedParticipants, String invitationModality,
    String cplReference, String cplByCostType, String curriculaFileName, String curriculaObjectKey, String curriculaSha256,
    LocalDate nominationDeadline) { }
