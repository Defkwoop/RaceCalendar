package com.teo.racecalendar.service;

import java.time.Clock;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teo.racecalendar.domain.RaceSession;
import com.teo.racecalendar.repository.RaceSessionRepository;

@Service
public class UpcomingSessionService {

    private final RaceSessionRepository sessionRepository;
    private final Clock clock = Clock.systemUTC();

    public UpcomingSessionService(
            RaceSessionRepository sessionRepository
    ) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional(readOnly = true)
    public List<UpcomingSessionDto> findUpcoming(int limit) {
        int safeLimit = Math.clamp(limit, 1, 100);

        return sessionRepository
                .findUpcoming(
                        clock.instant(),
                        PageRequest.of(0, safeLimit)
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

    private UpcomingSessionDto toDto(RaceSession session) {
        var round = session.getRound();
        var meeting = round.getMeeting();
        var venue = meeting.getVenue();
        var championship = round.getChampionship();

        return new UpcomingSessionDto(
                championship.getCode(),
                championship.getName(),
                meeting.getName(),
                venue.getName(),
                venue.getTimeZone(),
                session.getName(),
                session.getType(),
                session.getStartsAt(),
                session.getEndsAt(),
                session.getStatus(),
                session.getSourceUrl()
        );
    }
}
