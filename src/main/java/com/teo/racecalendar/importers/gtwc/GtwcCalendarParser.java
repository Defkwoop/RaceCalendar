package com.teo.racecalendar.importers.gtwc;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GtwcCalendarParser {

    private static final String BASE_URL
            = "https://www.gt-world-challenge-europe.com";

    private static final Pattern EVENT_PATH = Pattern.compile(
            "^/event/(\\d+)(?:/.*)?$"
    );

    public String findSeasonUrl(String calendarHtml, int season) {
        Document document = Jsoup.parse(calendarHtml, BASE_URL);

        Element seasonSelect = document.selectFirst(
                "select#filter_season_id"
        );

        if (seasonSelect == null) {
            throw new GtwcImportException(
                    "Could not find GTWC season selector"
            );
        }

        for (Element option : seasonSelect.select("option")) {
            if (option.text().trim().equals(Integer.toString(season))) {
                String seasonId = option.attr("value");

                return BASE_URL
                        + "/calendar?filter_season_id="
                        + seasonId;
            }
        }

        throw new GtwcImportException(
                "GTWC calendar does not contain season " + season
        );
    }

    public List<GtwcEventLink> parseEventLinks(String calendarHtml) {
        Document document = Jsoup.parse(calendarHtml, BASE_URL);

        Map<String, GtwcEventLink> uniqueEvents
                = new LinkedHashMap<>();

        for (Element link : document.select("a[href]")) {
            String absoluteUrl = link.absUrl("href");

            if (absoluteUrl.isBlank()) {
                continue;
            }

            URI uri;

            try {
                uri = URI.create(absoluteUrl);
            } catch (IllegalArgumentException exception) {
                continue;
            }

            Matcher matcher = EVENT_PATH.matcher(uri.getPath());

            if (!matcher.matches()) {
                continue;
            }

            String eventId = matcher.group(1);

            uniqueEvents.putIfAbsent(
                    eventId,
                    new GtwcEventLink(eventId, absoluteUrl)
            );
        }

        return new ArrayList<>(uniqueEvents.values());
    }
}
