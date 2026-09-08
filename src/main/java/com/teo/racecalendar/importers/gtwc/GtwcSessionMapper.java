package com.teo.racecalendar.importers.gtwc;

import com.teo.racecalendar.domain.enums.MeetingFormat;
import com.teo.racecalendar.domain.enums.RacingCategory;
import com.teo.racecalendar.domain.enums.ScheduleStatus;
import com.teo.racecalendar.domain.enums.SeriesCode;
import com.teo.racecalendar.domain.enums.SessionType;
import com.teo.racecalendar.importers.ImportedRound;
import com.teo.racecalendar.importers.ImportedSession;
import com.teo.racecalendar.importers.ImportedVenue;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class GtwcSessionMapper {

    private final GtwcVenueRegistry venueRegistry;

    public GtwcSessionMapper(
            GtwcVenueRegistry venueRegistry
    ) {
        this.venueRegistry = venueRegistry;
    }

    public ImportedRound map(
            GtwcEventPage event,
            int season
    ) {
        MeetingFormat format = determineFormat(event);
        ImportedVenue venue
                = venueRegistry.resolve(event.eventName());

        List<ImportedSession> sessions = event.sessions()
                .stream()
                .map(session -> new ImportedSession(
                session.sourceSessionId(),
                session.name(),
                determineSessionType(
                        session.name(),
                        format
                ),
                session.startsAt(),
                null,
                ScheduleStatus.PROVISIONAL,
                event.officialUrl()
        ))
                .toList();

        return new ImportedRound(
                SeriesCode.GTWC_EUROPE,
                "GT World Challenge Europe",
                RacingCategory.SPORTSCAR,
                season,
                event.roundNumber(),
                event.eventName(),
                format,
                "gtwc-europe-" + event.sourceEventId(),
                event.officialUrl(),
                event.eventName(),
                event.startDate(),
                event.endDate(),
                venue,
                sessions
        );
    }

    private MeetingFormat determineFormat(
            GtwcEventPage event
    ) {
        String eventName = event.eventName()
                .toLowerCase(Locale.ROOT);

        if (eventName.contains("test")
                || eventName.contains("prologue")) {
            return MeetingFormat.TEST;
        }

        boolean raceOne = containsSession(event, "race 1");
        boolean raceTwo = containsSession(event, "race 2");

        if (raceOne && raceTwo) {
            return MeetingFormat.SPRINT;
        }

        boolean containsRace = event.sessions()
                .stream()
                .map(GtwcSessionRow::name)
                .map(name -> name.toLowerCase(Locale.ROOT))
                .anyMatch(name -> name.contains("race"));

        if (containsRace) {
            return MeetingFormat.ENDURANCE;
        }

        return MeetingFormat.UNSPECIFIED;
    }

    private boolean containsSession(
            GtwcEventPage event,
            String expected
    ) {
        return event.sessions()
                .stream()
                .map(GtwcSessionRow::name)
                .map(name -> name.toLowerCase(Locale.ROOT))
                .anyMatch(name -> name.contains(expected));
    }

    private SessionType determineSessionType(
            String sessionName,
            MeetingFormat meetingFormat
    ) {
        String normalized
                = sessionName.toLowerCase(Locale.ROOT);

        if (normalized.contains("test")) {
            return SessionType.TEST;
        }

        if (normalized.contains("warm-up")
                || normalized.contains("warm up")) {
            return SessionType.WARM_UP;
        }

        if (normalized.contains("qualifying")) {
            return SessionType.QUALIFYING;
        }

        if (normalized.contains("practice")) {
            return SessionType.PRACTICE;
        }

        if (normalized.contains("race")) {
            return meetingFormat == MeetingFormat.SPRINT
                    ? SessionType.SPRINT
                    : SessionType.RACE;
        }

        return SessionType.OTHER;
    }
}
