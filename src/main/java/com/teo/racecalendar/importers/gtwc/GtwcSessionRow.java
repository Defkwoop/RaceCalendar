package com.teo.racecalendar.importers.gtwc;

import java.time.Instant;

public record GtwcSessionRow(
        String sourceSessionId,
        String name,
        Instant startsAt
        ) {

}
