package com.teo.racecalendar.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.teo.racecalendar.domain.Championship;
import com.teo.racecalendar.domain.ChampionshipRound;

public interface ChampionshipRoundRepository
        extends JpaRepository<ChampionshipRound, UUID> {

    Optional<ChampionshipRound>
            findByChampionshipAndSeasonAndSourceEventId(
                    Championship championship,
                    int season,
                    String sourceEventId
            );

    @Query("""
            select round
            from ChampionshipRound round
            join fetch round.championship championship
            join fetch round.meeting meeting
            join fetch meeting.venue venue
            where meeting.endDate >= :today
            order by meeting.startDate, round.roundNumber
            """)
    List<ChampionshipRound> findUpcoming(
            @Param("today") LocalDate today
    );
}
