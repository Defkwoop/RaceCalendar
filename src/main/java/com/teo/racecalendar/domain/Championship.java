package com.teo.racecalendar.domain;

import com.teo.racecalendar.domain.enums.RacingCategory;
import com.teo.racecalendar.domain.enums.SeriesCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "championship")
public class Championship extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private SeriesCode code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RacingCategory category;

    @Column(nullable = false)
    private boolean active = true;

    protected Championship() {
    }

    public Championship(
            SeriesCode code,
            String name,
            RacingCategory category
    ) {
        this.code = code;
        this.name = name;
        this.category = category;
    }

    public void updateMetadata(
            String name,
            RacingCategory category
    ) {
        this.name = name;
        this.category = category;
    }

    public SeriesCode getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public RacingCategory getCategory() {
        return category;
    }

    public boolean isActive() {
        return active;
    }
}
