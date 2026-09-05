package com.teo.racecalendar.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teo.racecalendar.domain.Championship;
import com.teo.racecalendar.domain.enums.SeriesCode;

public interface ChampionshipRepository
        extends JpaRepository<Championship, UUID> {

    Optional<Championship> findByCode(SeriesCode code);
}
