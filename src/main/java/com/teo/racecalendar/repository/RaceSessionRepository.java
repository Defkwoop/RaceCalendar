package com.teo.racecalendar.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.teo.racecalendar.domain.ChampionshipRound;
import com.teo.racecalendar.domain.RaceSession;

public interface RaceSessionRepository
        extends JpaRepository<RaceSession, UUID> {

    Optional<RaceSession> findByRoundAndSourceSessionId(
            ChampionshipRound round,
            String sourceSessionId
    );

    @Query("""
        select session
        from RaceSession session
        join fetch session.round round
        join fetch round.championship championship
        join fetch round.meeting meeting
        join fetch meeting.venue venue
        where session.startsAt >= :now
          and session.status <> com.teo.racecalendar.domain.enums.ScheduleStatus.CANCELLED
        order by session.startsAt
        """)
    List<RaceSession> findUpcoming(
            Instant now,
            Pageable pageable
    );
}
