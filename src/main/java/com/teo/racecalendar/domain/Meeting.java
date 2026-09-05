package com.teo.racecalendar.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "race_meeting")
public class Meeting extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String canonicalKey;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    protected Meeting() {
    }

    public Meeting(
            String canonicalKey,
            String name,
            Venue venue,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.canonicalKey = canonicalKey;
        this.name = name;
        this.venue = venue;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void update(
            String name,
            Venue venue,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.name = name;
        this.venue = venue;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getCanonicalKey() {
        return canonicalKey;
    }

    public String getName() {
        return name;
    }

    public Venue getVenue() {
        return venue;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
