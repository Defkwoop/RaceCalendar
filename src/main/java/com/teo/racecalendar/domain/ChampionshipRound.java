package com.teo.racecalendar.domain;

import com.teo.racecalendar.domain.enums.MeetingFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "championship_round")
public class ChampionshipRound extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "championship_id", nullable = false)
    private Championship championship;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(nullable = false)
    private int season;

    private Integer roundNumber;

    @Column(nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "weekend_format", nullable = false, length = 30)
    private MeetingFormat format;

    @Column(nullable = false)
    private String sourceEventId;

    @Column(nullable = false, length = 1000)
    private String officialUrl;

    protected ChampionshipRound() {
    }

    public ChampionshipRound(
            Championship championship,
            Meeting meeting,
            int season,
            Integer roundNumber,
            String displayName,
            MeetingFormat format,
            String sourceEventId,
            String officialUrl
    ) {
        this.championship = championship;
        this.meeting = meeting;
        this.season = season;
        this.roundNumber = roundNumber;
        this.displayName = displayName;
        this.format = format;
        this.sourceEventId = sourceEventId;
        this.officialUrl = officialUrl;
    }

    public void update(
            Meeting meeting,
            Integer roundNumber,
            String displayName,
            MeetingFormat format,
            String officialUrl
    ) {
        this.meeting = meeting;
        this.roundNumber = roundNumber;
        this.displayName = displayName;
        this.format = format;
        this.officialUrl = officialUrl;
    }

    public Championship getChampionship() {
        return championship;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public int getSeason() {
        return season;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public String getDisplayName() {
        return displayName;
    }

    public MeetingFormat getFormat() {
        return format;
    }

    public String getSourceEventId() {
        return sourceEventId;
    }

    public String getOfficialUrl() {
        return officialUrl;
    }
}
