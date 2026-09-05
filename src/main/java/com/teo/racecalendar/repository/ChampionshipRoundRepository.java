package com.teo.racecalendar.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

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
}
