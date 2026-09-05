package com.teo.racecalendar.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teo.racecalendar.domain.Venue;

public interface VenueRepository extends JpaRepository<Venue, UUID> {

    Optional<Venue> findBySlug(String slug);
}
