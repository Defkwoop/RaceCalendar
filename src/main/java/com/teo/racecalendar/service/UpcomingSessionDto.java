package com.teo.racecalendar.service;

import java.time.Instant;

import com.teo.racecalendar.domain.enums.ScheduleStatus;
import com.teo.racecalendar.domain.enums.SeriesCode;
import com.teo.racecalendar.domain.enums.SessionType;

public record UpcomingSessionDto(
        SeriesCode series,
        String championship,
        String meeting,
        String venue,
        String venueTimeZone,
        String session,
        SessionType type,
        Instant startsAt,
        Instant endsAt,
        ScheduleStatus status,
        String sourceUrl
        ) {

}
