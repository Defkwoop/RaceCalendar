package com.teo.racecalendar.importers.gtwc;

import java.time.Instant;

public record GtwcSessionRow(
        String sessionId,
        Instant startTime,
        Instant endTime
        ) {

}
