package com.teo.racecalendar.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teo.racecalendar.domain.Meeting;

public interface MeetingRepository extends JpaRepository<Meeting, UUID> {

    Optional<Meeting> findByCanonicalKey(String canonicalKey);
}
