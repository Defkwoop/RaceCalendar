package com.teo.racecalendar.importers;

public record ImportedVenue(
        String slug,
        String name,
        String countryCode,
        String timeZone
        ) {

}
