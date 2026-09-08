package com.teo.racecalendar.importers.gtwc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.teo.racecalendar.domain.enums.MeetingFormat;
import com.teo.racecalendar.domain.enums.SessionType;
import com.teo.racecalendar.importers.ImportedRound;

class GtwcEventParserTest {

    private final GtwcEventParser parser
            = new GtwcEventParser();

    private final GtwcSessionMapper mapper
            = new GtwcSessionMapper(
                    new GtwcVenueRegistry()
            );

    @Test
    void parsesSprintWeekend() throws IOException {
        GtwcEventPage event = parser.parse(
                new GtwcEventLink(
                        "253",
                        "https://example.test/event/253/zandvoort"
                ),
                loadFixture("europe-sprint-event.html")
        );

        assertThat(event.eventName())
                .isEqualTo("Zandvoort");

        assertThat(event.roundNumber())
                .isEqualTo(8);

        assertThat(event.startDate())
                .hasToString("2026-09-18");

        assertThat(event.endDate())
                .hasToString("2026-09-20");

        assertThat(event.sessions())
                .hasSize(7);

        assertThat(event.sessions())
                .anySatisfy(session -> {
                    assertThat(session.name())
                            .isEqualTo(
                                    "Official Paid Test Session 1"
                            );

                    assertThat(session.startsAt())
                            .isEqualTo(
                                    Instant.parse(
                                            "2026-09-17T12:00:00Z"
                                    )
                            );
                });

        ImportedRound round = mapper.map(event, 2026);

        assertThat(round.format())
                .isEqualTo(MeetingFormat.SPRINT);

        assertThat(round.sessions())
                .filteredOn(
                        session
                        -> session.type()
                        == SessionType.SPRINT
                )
                .hasSize(2);
    }

    @Test
    void parsesEnduranceWeekend()
            throws IOException {

        GtwcEventPage event = parser.parse(
                new GtwcEventLink(
                        "252",
                        "https://example.test/event/252/nurburgring"
                ),
                loadFixture(
                        "europe-endurance-event.html"
                )
        );

        /*
         * The checkpoint row must not become a session,
         * so there should be six sessions rather than seven.
         */
        assertThat(event.sessions()).hasSize(6);

        GtwcSessionRow mainRace = event.sessions()
                .stream()
                .filter(session
                        -> session.name().equals("Main Race"))
                .findFirst()
                .orElseThrow();

        assertThat(mainRace.startsAt())
                .isEqualTo(
                        Instant.parse(
                                "2026-08-30T13:00:00Z"
                        )
                );

        assertThat(event.sessions())
                .noneMatch(session
                        -> session.name().contains("after")
                );

        ImportedRound round = mapper.map(event, 2026);

        assertThat(round.format())
                .isEqualTo(MeetingFormat.ENDURANCE);
    }

    @Test
    void acceptsEventWithoutTimetable()
            throws IOException {

        GtwcEventPage event = parser.parse(
                new GtwcEventLink(
                        "255",
                        "https://example.test/event/255/portimao"
                ),
                loadFixture(
                        "event-without-timetable.html"
                )
        );

        assertThat(event.eventName())
                .isEqualTo("Portimao");

        assertThat(event.roundNumber())
                .isEqualTo(10);

        assertThat(event.sessions()).isEmpty();

        ImportedRound round = mapper.map(event, 2026);

        assertThat(round.format())
                .isEqualTo(
                        MeetingFormat.UNSPECIFIED
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
