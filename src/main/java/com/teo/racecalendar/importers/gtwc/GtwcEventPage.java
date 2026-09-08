package com.teo.racecalendar.importers.gtwc;

import java.time.LocalDate;
import java.util.List;

public record GtwcEventPage(
        String sourceEventId,
        String officialUrl,
        String eventName,
        String countryName,
        Integer roundNumber,
        LocalDate startDate,
        LocalDate endDate,
        List<GtwcSessionRow> sessions
        ) {

}
