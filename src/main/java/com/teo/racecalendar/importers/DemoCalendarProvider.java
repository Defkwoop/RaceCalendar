package com.teo.racecalendar.importers;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import com.teo.racecalendar.domain.enums.MeetingFormat;
import com.teo.racecalendar.domain.enums.RacingCategory;
import com.teo.racecalendar.domain.enums.ScheduleStatus;
import com.teo.racecalendar.domain.enums.SeriesCode;
import com.teo.racecalendar.domain.enums.SessionType;

@Component
public class DemoCalendarProvider implements CalendarProvider {

    @Override
    public String providerId() {
        return "demo-gtwc-europe";
    }

    @Override
    public List<ImportedRound> fetchSeason(int season) {
        if (season != 2026) {
            return List.of();
        }

        String eventUrl
                = "https://www.gt-world-challenge-europe.com/event/254/barcelona";

        ImportedVenue venue = new ImportedVenue(
                "circuit-de-barcelona-catalunya",
                "Circuit de Barcelona-Catalunya",
                "ES",
                "Europe/Madrid"
        );

        List<ImportedSession> sessions = List.of(
                new ImportedSession(
                        "race-1",
                        "Race 1",
                        SessionType.RACE,
                        Instant.parse("2026-10-03T12:00:00Z"),
                        null,
                        ScheduleStatus.PROVISIONAL,
                        eventUrl
                ),
                new ImportedSession(
                        "race-2",
                        "Race 2",
                        SessionType.RACE,
                        Instant.parse("2026-10-04T14:00:00Z"),
                        null,
                        ScheduleStatus.PROVISIONAL,
                        eventUrl
                )
        );

        ImportedRound barcelona = new ImportedRound(
                SeriesCode.GTWC_EUROPE,
                "GT World Challenge Europe",
                RacingCategory.SPORTSCAR,
                2026,
                9,
                "Barcelona",
                MeetingFormat.SPRINT,
                "254",
                eventUrl,
                "Barcelona GT Weekend",
                LocalDate.of(2026, 10, 2),
                LocalDate.of(2026, 10, 4),
                venue,
                sessions
        );

        return List.of(barcelona);
    }
}
