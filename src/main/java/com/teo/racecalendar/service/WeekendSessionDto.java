package com.teo.racecalendar.service;

import java.time.Instant;

import com.teo.racecalendar.domain.enums.ScheduleStatus;
import com.teo.racecalendar.domain.enums.SessionType;

public record WeekendSessionDto(
        String name,
        SessionType type,
        Instant startsAt,
        Instant endsAt,
        ScheduleStatus status
        ) {

    public boolean isRace() {
        return type == SessionType.RACE
                || type == SessionType.SPRINT;
    }

}
