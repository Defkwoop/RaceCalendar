package com.teo.racecalendar.domain;

import java.time.Instant;
import java.util.Objects;

import com.teo.racecalendar.domain.enums.ScheduleStatus;
import com.teo.racecalendar.domain.enums.SessionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "race_session")
public class RaceSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "round_id", nullable = false)
    private ChampionshipRound round;

    @Column(nullable = false)
    private String sourceSessionId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SessionType type;

    @Column(nullable = false)
    private Instant startsAt;

    private Instant endsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ScheduleStatus status;

    @Column(nullable = false, length = 1000)
    private String sourceUrl;

    @Column(nullable = false)
    private Instant lastSeenAt;

    @Column(nullable = false)
    private Instant lastChangedAt;

    protected RaceSession() {
    }

    public RaceSession(
            ChampionshipRound round,
            String sourceSessionId,
            String name,
            SessionType type,
            Instant startsAt,
            Instant endsAt,
            ScheduleStatus status,
            String sourceUrl
    ) {
        this.round = round;
        this.sourceSessionId = sourceSessionId;
        this.name = name;
        this.type = type;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.status = status;
        this.sourceUrl = sourceUrl;

        Instant now = Instant.now();
        this.lastSeenAt = now;
        this.lastChangedAt = now;
    }

    public boolean updateFromSource(
            String name,
            SessionType type,
            Instant startsAt,
            Instant endsAt,
            ScheduleStatus status,
            String sourceUrl
    ) {
        boolean changed
                = !Objects.equals(this.name, name)
                || this.type != type
                || !Objects.equals(this.startsAt, startsAt)
                || !Objects.equals(this.endsAt, endsAt)
                || this.status != status;

        this.name = name;
        this.type = type;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.status = status;
        this.sourceUrl = sourceUrl;
        this.lastSeenAt = Instant.now();

        if (changed) {
            this.lastChangedAt = Instant.now();
        }

        return changed;
    }

    public ChampionshipRound getRound() {
        return round;
    }

    public String getSourceSessionId() {
        return sourceSessionId;
    }

    public String getName() {
        return name;
    }

    public SessionType getType() {
        return type;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public Instant getLastChangedAt() {
        return lastChangedAt;
    }
}
