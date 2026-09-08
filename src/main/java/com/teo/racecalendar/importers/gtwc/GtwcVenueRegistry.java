package com.teo.racecalendar.importers.gtwc;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.teo.racecalendar.importers.ImportedVenue;

@Component
public class GtwcVenueRegistry {

    private final Map<String, VenueDefinition> venues = Map.ofEntries(
            entry("Circuit Paul Ricard",
                    "circuit-paul-ricard",
                    "Circuit Paul Ricard",
                    "FR",
                    "Europe/Paris"),
            entry("Brands Hatch",
                    "brands-hatch",
                    "Brands Hatch",
                    "GB",
                    "Europe/London"),
            entry("Monza",
                    "monza",
                    "Monza",
                    "IT",
                    "Europe/Rome"),
            entry("Imola",
                    "imola",
                    "Autodromo Internazionale Enzo e Dino Ferrari",
                    "IT",
                    "Europe/Rome"),
            entry("Budapest",
                    "hungaroring",
                    "Hungaroring",
                    "HU",
                    "Europe/Budapest"),
            entry("CrowdStrike 24 Hours of Spa",
                    "spa-francorchamps",
                    "Circuit de Spa-Francorchamps",
                    "BE",
                    "Europe/Brussels"),
            entry("Misano",
                    "misano",
                    "Misano World Circuit",
                    "IT",
                    "Europe/Rome"),
            entry("Magny-Cours",
                    "magny-cours",
                    "Circuit de Nevers Magny-Cours",
                    "FR",
                    "Europe/Paris"),
            entry("Nürburgring",
                    "nurburgring",
                    "Nürburgring",
                    "DE",
                    "Europe/Berlin"),
            entry("Zandvoort",
                    "zandvoort",
                    "Circuit Zandvoort",
                    "NL",
                    "Europe/Amsterdam"),
            entry("Barcelona",
                    "barcelona",
                    "Circuit de Barcelona-Catalunya",
                    "ES",
                    "Europe/Madrid"),
            entry("Portimao",
                    "portimao",
                    "Algarve International Circuit",
                    "PT",
                    "Europe/Lisbon")
    );

    public ImportedVenue resolve(String eventName) {
        VenueDefinition venue = venues.get(normalize(eventName));

        if (venue == null) {
            throw new GtwcImportException(
                    "Unknown GTWC venue/event name: "
                    + eventName
                    + ". Add it to GtwcVenueRegistry."
            );
        }

        return new ImportedVenue(
                venue.slug(),
                venue.name(),
                venue.countryCode(),
                venue.timeZone()
        );
    }

    private Map.Entry<String, VenueDefinition> entry(
            String sourceName,
            String slug,
            String canonicalName,
            String countryCode,
            String timeZone
    ) {
        return Map.entry(
                normalize(sourceName),
                new VenueDefinition(
                        slug,
                        canonicalName,
                        countryCode,
                        timeZone
                )
        );
    }

    private String normalize(String value) {
        return Normalizer
                .normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    private record VenueDefinition(
            String slug,
            String name,
            String countryCode,
            String timeZone
            ) {

    }
}
