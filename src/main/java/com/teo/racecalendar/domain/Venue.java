package com.teo.racecalendar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "venue")
public class Venue extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2)
    private String countryCode;

    @Column(nullable = false)
    private String timeZone;

    protected Venue() {
    }

    public Venue(
            String slug,
            String name,
            String countryCode,
            String timeZone
    ) {
        this.slug = slug;
        this.name = name;
        this.countryCode = countryCode;
        this.timeZone = timeZone;
    }

    public void update(
            String name,
            String countryCode,
            String timeZone
    ) {
        this.name = name;
        this.countryCode = countryCode;
        this.timeZone = timeZone;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getTimeZone() {
        return timeZone;
    }
}
