package com.teo.racecalendar.service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teo.racecalendar.domain.ChampionshipRound;
import com.teo.racecalendar.domain.Meeting;
import com.teo.racecalendar.domain.RaceSession;
import com.teo.racecalendar.domain.Venue;
import com.teo.racecalendar.repository.ChampionshipRoundRepository;
import com.teo.racecalendar.repository.RaceSessionRepository;

@Service
public class WeekendCalendarService {

    private final ChampionshipRoundRepository roundRepository;
    private final RaceSessionRepository sessionRepository;

    public WeekendCalendarService(
            ChampionshipRoundRepository roundRepository,
            RaceSessionRepository sessionRepository
    ) {
        this.roundRepository = roundRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional(readOnly = true)
    public List<WeekendCalendarDto> findUpcomingWeekends() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        List<ChampionshipRound> rounds
                = roundRepository.findUpcoming(today);

        if (rounds.isEmpty()) {
            return List.of();
        }

        List<RaceSession> sessions
                = sessionRepository
                        .findByRoundInOrderByStartsAtAsc(rounds);

        Map<UUID, List<RaceSession>> sessionsByRound
                = sessions.stream()
                        .collect(
                                Collectors.groupingBy(
                                        session
                                        -> session.getRound()
                                                .getId()
                                )
                        );

        return rounds.stream()
                .map(round -> toDto(
                round,
                sessionsByRound.getOrDefault(
                        round.getId(),
                        List.of()
                )
        ))
                .toList();
    }

    private WeekendCalendarDto toDto(
            ChampionshipRound round,
            List<RaceSession> sessions
    ) {
        Meeting meeting = round.getMeeting();
        Venue venue = meeting.getVenue();

        List<WeekendSessionDto> sessionDtos
                = sessions.stream()
                        .map(this::toSessionDto)
                        .toList();

        return new WeekendCalendarDto(
                round.getId().toString(),
                round.getChampionship().getCode(),
                round.getChampionship().getName(),
                round.getSeason(),
                round.getRoundNumber(),
                round.getDisplayName(),
                venue.getName(),
                venue.getTimeZone(),
                meeting.getStartDate(),
                meeting.getEndDate(),
                round.getFormat(),
                round.getOfficialUrl(),
                sessionDtos
        );
    }

    private WeekendSessionDto toSessionDto(
            RaceSession session
    ) {
        return new WeekendSessionDto(
                session.getName(),
                session.getType(),
                session.getStartsAt(),
                session.getEndsAt(),
                session.getStatus()
        );
    }
}
