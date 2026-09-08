package com.teo.racecalendar.importers.gtwc;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class GtwcEventParser {

    private static final Pattern ROUND_PATTERN
            = Pattern.compile("(?i)^Round\\s+(\\d+)$");

    private static final Pattern SAME_MONTH_DATE_PATTERN
            = Pattern.compile(
                    "^(\\d{1,2})\\s*-\\s*(\\d{1,2})"
                    + "\\s+([A-Za-z]+)\\s+(\\d{4})$"
            );

    private static final Pattern DIFFERENT_MONTH_DATE_PATTERN
            = Pattern.compile(
                    "^(\\d{1,2})\\s+([A-Za-z]+)\\s+(\\d{4})"
                    + "\\s*-\\s*"
                    + "(\\d{1,2})\\s+([A-Za-z]+)\\s+(\\d{4})$"
            );

    private static final DateTimeFormatter TABLE_DAY_FORMAT
            = DateTimeFormatter.ofPattern(
                    "EEEE, d MMMM",
                    Locale.ENGLISH
            );

    private static final DateTimeFormatter TIME_FORMAT
            = DateTimeFormatter.ofPattern(
                    "H:mm",
                    Locale.ENGLISH
            );

    public GtwcEventPage parse(GtwcEventLink eventLink) {
        throw new UnsupportedOperationException(
                "Use parse(eventLink, html)"
        );
    }

    public GtwcEventPage parse(
            GtwcEventLink eventLink,
            String html
    ) {
        Document document = Jsoup.parse(html, eventLink.url());

        String eventName = requiredText(
                document,
                ".feature__heading",
                "event name"
        );

        String countryName = requiredText(
                document,
                ".feature__subheading-text",
                "country"
        );

        Integer roundNumber = null;
        String dateText = null;

        for (Element element
                : document.select(".feature__list-item-span")) {

            String text = element.text().trim();

            Matcher roundMatcher = ROUND_PATTERN.matcher(text);

            if (roundMatcher.matches()) {
                roundNumber = Integer.parseInt(
                        roundMatcher.group(1)
                );
            }

            if (text.matches(".*\\b\\d{4}\\b.*")) {
                dateText = text;
            }
        }

        if (dateText == null) {
            throw new GtwcImportException(
                    "No event date found for " + eventLink.url()
            );
        }

        DateRange dateRange = parseDateRange(dateText);

        List<GtwcSessionRow> sessions = parseSessions(
                document,
                eventLink.eventId(),
                dateRange.start(),
                dateRange.end()
        );

        return new GtwcEventPage(
                eventLink.eventId(),
                eventLink.url(),
                eventName,
                countryName,
                roundNumber,
                dateRange.start(),
                dateRange.end(),
                sessions
        );
    }

    private List<GtwcSessionRow> parseSessions(
            Document document,
            String eventId,
            LocalDate eventStart,
            LocalDate eventEnd
    ) {
        List<GtwcSessionRow> sessions = new ArrayList<>();
        Map<String, Integer> nameOccurrences = new HashMap<>();

        for (Element table
                : document.select("table.timetable__table")) {

            Element caption = table.selectFirst("caption");

            if (caption == null) {
                continue;
            }

            LocalDate sessionDate = parseSessionDate(
                    caption.text().trim(),
                    eventStart,
                    eventEnd
            );

            for (Element row : table.select("tbody tr")) {
                Elements cells = row.select("td");

                if (cells.size() < 3) {
                    continue;
                }

                String sessionName = cells.get(0).text().trim();
                String gmtTime = cells.get(2).text().trim();

                if (sessionName.isBlank() || gmtTime.isBlank()) {
                    continue;
                }

                /*
                 * GTWC sometimes lists race progress checkpoints such as:
                 * "Main Race after 0.5 h".
                 *
                 * Those are not separate sessions.
                 */
                if (sessionName.toLowerCase(Locale.ROOT)
                        .contains(" after ")) {
                    continue;
                }

                LocalTime time;

                try {
                    time = LocalTime.parse(gmtTime, TIME_FORMAT);
                } catch (DateTimeParseException exception) {
                    continue;
                }

                Instant startsAt = sessionDate
                        .atTime(time)
                        .toInstant(ZoneOffset.UTC);

                String normalizedName = normalize(sessionName);

                int occurrence = nameOccurrences.merge(
                        normalizedName,
                        1,
                        Integer::sum
                );

                /*
                 * Do not include the start time in this ID.
                 *
                 * If GTWC changes 15:00 to 15:30, the ID must remain
                 * stable so Phase 3 updates the existing row instead
                 * of inserting a duplicate.
                 */
                String sourceSessionId = eventId
                        + ":"
                        + normalizedName
                        + ":"
                        + occurrence;

                sessions.add(new GtwcSessionRow(
                        sourceSessionId,
                        sessionName,
                        startsAt
                ));
            }
        }

        return sessions;
    }

    private LocalDate parseSessionDate(
            String caption,
            LocalDate eventStart,
            LocalDate eventEnd
    ) {
        java.time.MonthDay parsedDay;

        try {
            parsedDay = java.time.MonthDay.parse(
                    caption,
                    TABLE_DAY_FORMAT
            );
        } catch (DateTimeParseException exception) {
            throw new GtwcImportException(
                    "Unsupported GTWC timetable date: "
                    + caption,
                    exception
            );
        }

        /*
     * GTWC may advertise a Friday-Sunday race weekend
     * while official tests begin on Thursday.
     *
     * Allow timetable sessions up to seven days before
     * the advertised start and one day after its end.
         */
        LocalDate permittedStart
                = eventStart.minusDays(7);

        LocalDate permittedEnd
                = eventEnd.plusDays(1);

        for (int year = permittedStart.getYear();
                year <= permittedEnd.getYear();
                year++) {

            LocalDate candidate = parsedDay.atYear(year);

            if (!candidate.isBefore(permittedStart)
                    && !candidate.isAfter(permittedEnd)) {
                return candidate;
            }
        }

        throw new GtwcImportException(
                "Timetable date '" + caption
                + "' is outside the permitted range "
                + permittedStart
                + " to "
                + permittedEnd
        );
    }

    private DateRange parseDateRange(String text) {
        String cleaned = text.replaceAll("\\s+", " ").trim();

        Matcher sameMonth
                = SAME_MONTH_DATE_PATTERN.matcher(cleaned);

        if (sameMonth.matches()) {
            int startDay = Integer.parseInt(sameMonth.group(1));
            int endDay = Integer.parseInt(sameMonth.group(2));
            Month month = parseMonth(sameMonth.group(3));
            int year = Integer.parseInt(sameMonth.group(4));

            return new DateRange(
                    LocalDate.of(year, month, startDay),
                    LocalDate.of(year, month, endDay)
            );
        }

        Matcher differentMonth
                = DIFFERENT_MONTH_DATE_PATTERN.matcher(cleaned);

        if (differentMonth.matches()) {
            return new DateRange(
                    LocalDate.of(
                            Integer.parseInt(differentMonth.group(3)),
                            parseMonth(differentMonth.group(2)),
                            Integer.parseInt(differentMonth.group(1))
                    ),
                    LocalDate.of(
                            Integer.parseInt(differentMonth.group(6)),
                            parseMonth(differentMonth.group(5)),
                            Integer.parseInt(differentMonth.group(4))
                    )
            );
        }

        throw new GtwcImportException(
                "Unsupported GTWC date format: " + text
        );
    }

    private Month parseMonth(String value) {
        return Month.from(
                DateTimeFormatter.ofPattern(
                        "MMMM",
                        Locale.ENGLISH
                ).parse(value)
        );
    }

    private String requiredText(
            Document document,
            String selector,
            String description
    ) {
        Element element = document.selectFirst(selector);

        if (element == null || element.text().isBlank()) {
            throw new GtwcImportException(
                    "Could not find GTWC " + description
            );
        }

        return element.text().trim();
    }

    private String normalize(String value) {
        return java.text.Normalizer
                .normalize(
                        value,
                        java.text.Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private record DateRange(
            LocalDate start,
            LocalDate end
            ) {

    }
}
