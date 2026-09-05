package com.teo.racecalendar.importers;

import java.time.Instant;

import com.teo.racecalendar.domain.enums.ScheduleStatus;
import com.teo.racecalendar.domain.enums.SessionType;

public record ImportedSession(
        String sourceSessionId,
        String name,
        SessionType type,
        Instant startsAt,
        Instant endsAt,
        ScheduleStatus status,
        String sourceUrl
        ) {

}
