package com.teo.racecalendar.service;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teo.racecalendar.domain.Championship;
import com.teo.racecalendar.domain.ChampionshipRound;
import com.teo.racecalendar.domain.Meeting;
import com.teo.racecalendar.domain.RaceSession;
import com.teo.racecalendar.domain.Venue;
import com.teo.racecalendar.importers.CalendarProvider;
import com.teo.racecalendar.importers.ImportedRound;
import com.teo.racecalendar.importers.ImportedSession;
import com.teo.racecalendar.importers.ImportedVenue;
import com.teo.racecalendar.repository.ChampionshipRepository;
import com.teo.racecalendar.repository.ChampionshipRoundRepository;
import com.teo.racecalendar.repository.MeetingRepository;
import com.teo.racecalendar.repository.RaceSessionRepository;
import com.teo.racecalendar.repository.VenueRepository;

@Service
public class CalendarImportService {

    private final ChampionshipRepository championshipRepository;
    private final VenueRepository venueRepository;
    private final MeetingRepository meetingRepository;
    private final ChampionshipRoundRepository roundRepository;
    private final RaceSessionRepository sessionRepository;

    public CalendarImportService(
            ChampionshipRepository championshipRepository,
            VenueRepository venueRepository,
            MeetingRepository meetingRepository,
            ChampionshipRoundRepository roundRepository,
            RaceSessionRepository sessionRepository
    ) {
        this.championshipRepository = championshipRepository;
        this.venueRepository = venueRepository;
        this.meetingRepository = meetingRepository;
        this.roundRepository = roundRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public void importSeason(
            CalendarProvider provider,
            int season
    ) {
        for (ImportedRound importedRound : provider.fetchSeason(season)) {
            importRound(importedRound);
        }
    }

    private void importRound(ImportedRound imported) {
        Championship championship = championshipRepository
                .findByCode(imported.seriesCode())
                .orElseGet(() -> championshipRepository.save(
                new Championship(
                        imported.seriesCode(),
                        imported.championshipName(),
                        imported.category()
                )
        ));

        championship.updateMetadata(
                imported.championshipName(),
                imported.category()
        );

        Venue venue = upsertVenue(imported.venue());

        String meetingKey = buildMeetingKey(imported);

        Meeting meeting = meetingRepository
                .findByCanonicalKey(meetingKey)
                .orElseGet(() -> meetingRepository.save(
                new Meeting(
                        meetingKey,
                        imported.meetingName(),
                        venue,
                        imported.meetingStart(),
                        imported.meetingEnd()
                )
        ));

        meeting.update(
                imported.meetingName(),
                venue,
                imported.meetingStart(),
                imported.meetingEnd()
        );

        ChampionshipRound round = roundRepository
                .findByChampionshipAndSeasonAndSourceEventId(
                        championship,
                        imported.season(),
                        imported.sourceEventId()
                )
                .orElseGet(() -> roundRepository.save(
                new ChampionshipRound(
                        championship,
                        meeting,
                        imported.season(),
                        imported.roundNumber(),
                        imported.roundName(),
                        imported.format(),
                        imported.sourceEventId(),
                        imported.officialUrl()
                )
        ));

        round.update(
                meeting,
                imported.roundNumber(),
                imported.roundName(),
                imported.format(),
                imported.officialUrl()
        );

        for (ImportedSession importedSession : imported.sessions()) {
            upsertSession(round, importedSession);
        }
    }

    private Venue upsertVenue(ImportedVenue imported) {
        Venue venue = venueRepository
                .findBySlug(imported.slug())
                .orElseGet(() -> venueRepository.save(
                new Venue(
                        imported.slug(),
                        imported.name(),
                        imported.countryCode(),
                        imported.timeZone()
                )
        ));

        venue.update(
                imported.name(),
                imported.countryCode(),
                imported.timeZone()
        );

        return venue;
    }

    private void upsertSession(
            ChampionshipRound round,
            ImportedSession imported
    ) {
        RaceSession session = sessionRepository
                .findByRoundAndSourceSessionId(
                        round,
                        imported.sourceSessionId()
                )
                .orElseGet(() -> sessionRepository.save(
                new RaceSession(
                        round,
                        imported.sourceSessionId(),
                        imported.name(),
                        imported.type(),
                        imported.startsAt(),
                        imported.endsAt(),
                        imported.status(),
                        imported.sourceUrl()
                )
        ));

        session.updateFromSource(
                imported.name(),
                imported.type(),
                imported.startsAt(),
                imported.endsAt(),
                imported.status(),
                imported.sourceUrl()
        );
    }

    private String buildMeetingKey(ImportedRound imported) {
        return String.join(
                ":",
                imported.venue().slug().toLowerCase(Locale.ROOT),
                imported.meetingStart().toString()
        );
    }
}
