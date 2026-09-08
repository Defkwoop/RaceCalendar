package com.teo.racecalendar.importers.gtwc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class GtwcCalendarParserTest {

    private final GtwcCalendarParser parser
            = new GtwcCalendarParser();

    @Test
    void findsSeasonUrl() throws IOException {
        String html = loadFixture(
                "europe-calendar-2026.html"
        );

        String seasonUrl = parser.findSeasonUrl(
                html,
                2026
        );

        assertThat(seasonUrl).isEqualTo(
                "https://www.gt-world-challenge-europe.com"
                + "/calendar?filter_season_id=26"
        );
    }

    @Test
    void findsEventLinks() throws IOException {
        String html = loadFixture(
                "europe-calendar-2026.html"
        );

        List<GtwcEventLink> events
                = parser.parseEventLinks(html);

        assertThat(events).hasSize(2);

        assertThat(events)
                .extracting(GtwcEventLink::eventId)
                .containsExactly("253", "254");

        assertThat(events.getFirst().url())
                .isEqualTo(
                        "https://www.gt-world-challenge-europe.com"
                        + "/event/253/zandvoort"
                );
    }

    private String loadFixture(String filename)
            throws IOException {

        String resourcePath
                = "/fixtures/gtwc/" + filename;

        try (InputStream stream
                = Objects.requireNonNull(
                        getClass().getResourceAsStream(
                                resourcePath
                        ),
                        "Fixture not found: "
                        + resourcePath
                )) {

                    return new String(
                            stream.readAllBytes(),
                            StandardCharsets.UTF_8
                    );
                }
    }
}
