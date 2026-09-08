package com.teo.racecalendar.importers.gtwc;

import com.teo.racecalendar.importers.CalendarProvider;
import com.teo.racecalendar.importers.ImportedRound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GtwcEuropeProvider
        implements CalendarProvider {

    private static final Logger log
            = LoggerFactory.getLogger(
                    GtwcEuropeProvider.class
            );

    private static final String CALENDAR_URL
            = "https://www.gt-world-challenge-europe.com/calendar";

    private final GtwcHttpClient httpClient;
    private final GtwcCalendarParser calendarParser;
    private final GtwcEventParser eventParser;
    private final GtwcSessionMapper sessionMapper;

    public GtwcEuropeProvider(
            GtwcHttpClient httpClient,
            GtwcCalendarParser calendarParser,
            GtwcEventParser eventParser,
            GtwcSessionMapper sessionMapper
    ) {
        this.httpClient = httpClient;
        this.calendarParser = calendarParser;
        this.eventParser = eventParser;
        this.sessionMapper = sessionMapper;
    }

    @Override
    public String providerId() {
        return "gtwc-europe";
    }

    @Override
    public List<ImportedRound> fetchSeason(int season) {
        String landingHtml = httpClient.get(CALENDAR_URL);

        String seasonUrl = calendarParser.findSeasonUrl(
                landingHtml,
                season
        );

        String seasonHtml = httpClient.get(seasonUrl);

        List<GtwcEventLink> eventLinks
                = calendarParser.parseEventLinks(seasonHtml);

        if (eventLinks.isEmpty()) {
            log.info(
                    "GTWC calendar currently contains no events for season={}",
                    season
            );

            return List.of();
        }

        List<ImportedRound> rounds = new ArrayList<>();

        for (GtwcEventLink eventLink : eventLinks) {
            try {
                String eventHtml
                        = httpClient.get(eventLink.url());

                GtwcEventPage event = eventParser.parse(
                        eventLink,
                        eventHtml
                );

                /*
                 * Prologues and test days currently have no round
                 * number. Skip them for the first version.
                 */
                if (event.roundNumber() == null) {
                    log.info(
                            "Skipping non-round GTWC event: {}",
                            event.eventName()
                    );
                    continue;
                }

                rounds.add(sessionMapper.map(event, season));

                pauseBetweenRequests();
            } catch (RuntimeException exception) {
                /*
                 * One broken event should not prevent the rest of
                 * the championship from updating.
                 */
                log.error(
                        "Could not import GTWC event {} from {}",
                        eventLink.eventId(),
                        eventLink.url(),
                        exception
                );
            }
        }

        if (rounds.isEmpty()) {
            throw new GtwcImportException(
                    "No GTWC rounds could be imported for "
                    + season
            );
        }

        return rounds;
    }

    private void pauseBetweenRequests() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new GtwcImportException(
                    "GTWC import was interrupted",
                    exception
            );
        }
    }
}
