package com.teo.racecalendar.ui;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Value;

import com.teo.racecalendar.service.WeekendCalendarDto;
import com.teo.racecalendar.service.WeekendCalendarService;
import com.teo.racecalendar.service.WeekendSessionDto;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("Race Calendar")
public class DashboardView extends VerticalLayout {

    private static final DateTimeFormatter WEEKEND_DATE_FORMAT
            = DateTimeFormatter.ofPattern("d MMM yyyy");

    private static final DateTimeFormatter DAY_HEADING_FORMAT
            = DateTimeFormatter.ofPattern("EEEE, d MMMM");

    private static final DateTimeFormatter SESSION_TIME_FORMAT
            = DateTimeFormatter.ofPattern("HH:mm");

    private static final DateTimeFormatter RACE_TIME_FORMAT
            = DateTimeFormatter.ofPattern("EEE HH:mm");

    private final ZoneId displayZone;

    public DashboardView(
            WeekendCalendarService weekendService,
            @Value("${app.display-zone:Europe/Prague}") String displayZone
    ) {
        this.displayZone = ZoneId.of(displayZone);

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(
                new H1("Race Calendar"),
                new Paragraph(
                        "Upcoming race weekends. Times shown in "
                        + this.displayZone
                ),
                new Paragraph(
                        "Click a weekend to view practice, "
                        + "qualifying and the full timetable."
                )
        );

        Grid<WeekendCalendarDto> grid
                = createWeekendGrid();

        grid.setItems(
                weekendService.findUpcomingWeekends()
        );

        grid.setSizeFull();

        add(grid);
        setFlexGrow(1, grid);
    }

    private Grid<WeekendCalendarDto> createWeekendGrid() {
        Grid<WeekendCalendarDto> grid
                = new Grid<>(WeekendCalendarDto.class, false);

        grid.addColumn(this::formatChampionship)
                .setHeader("Championship")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(this::formatEventName)
                .setHeader("Weekend")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(this::formatWeekendDates)
                .setHeader("Dates")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(this::formatWeekendType)
                .setHeader("Format")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(
                new ComponentRenderer<>(
                        this::createRaceSummary
                )
        )
                .setHeader("Race time")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(this::formatRaceCountdown)
                .setHeader("Next race")
                .setAutoWidth(true)
                .setFlexGrow(0);

        /*
         * Phase 5G:
         *
         * Vaadin renders this component underneath the
         * selected weekend row.
         */
        grid.setItemDetailsRenderer(
                new ComponentRenderer<>(
                        this::createSessionDetails
                )
        );

        /*
         * Clicking anywhere on the row opens or closes
         * the details section.
         */
        grid.setDetailsVisibleOnClick(true);

        return grid;
    }

    private String formatChampionship(
            WeekendCalendarDto weekend
    ) {
        return weekend.championship();
    }

    private String formatEventName(
            WeekendCalendarDto weekend
    ) {
        if (weekend.roundNumber() == null) {
            return weekend.eventName();
        }

        return weekend.eventName()
                + " · Round "
                + weekend.roundNumber();
    }

    private String formatWeekendDates(
            WeekendCalendarDto weekend
    ) {
        LocalDate start = weekend.startDate();
        LocalDate end = weekend.endDate();

        if (start.equals(end)) {
            return WEEKEND_DATE_FORMAT.format(start);
        }

        if (start.getYear() == end.getYear()
                && start.getMonth() == end.getMonth()) {

            return start.getDayOfMonth()
                    + "–"
                    + end.getDayOfMonth()
                    + " "
                    + end.getMonth()
                            .toString()
                            .substring(0, 3)
                    + " "
                    + end.getYear();
        }

        return WEEKEND_DATE_FORMAT.format(start)
                + " – "
                + WEEKEND_DATE_FORMAT.format(end);
    }

    private String formatWeekendType(
            WeekendCalendarDto weekend
    ) {
        return switch (weekend.format()) {
            case SPRINT ->
                "Sprint";
            case ENDURANCE ->
                "Endurance";
            case STREET ->
                "Street";
            case OVAL ->
                "Oval";
            case ROAD_COURSE ->
                "Road course";
            case TEST ->
                "Test";
            case UNSPECIFIED ->
                "TBA";
        };
    }

    /**
     * This creates the information visible while the weekend row is collapsed.
     *
     * Only race sessions are shown here.
     */
    private Component createRaceSummary(
            WeekendCalendarDto weekend
    ) {
        VerticalLayout raceList = new VerticalLayout();

        raceList.setPadding(false);
        raceList.setSpacing(false);

        List<WeekendSessionDto> races
                = weekend.sessions()
                        .stream()
                        .filter(WeekendSessionDto::isRace)
                        .filter(session
                                -> session.startsAt() != null
                        )
                        .sorted((first, second)
                                -> first.startsAt().compareTo(
                                second.startsAt()
                        )
                        )
                        .toList();

        if (races.isEmpty()) {
            Span tba = new Span("Race time TBA");

            tba.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-style", "italic");

            raceList.add(tba);

            return raceList;
        }

        for (WeekendSessionDto race : races) {
            Div raceLine = new Div();

            Span name = new Span(race.name() + ": ");

            name.getStyle().set("font-weight", "600");

            Span start = new Span(
                    RACE_TIME_FORMAT.format(
                            race.startsAt()
                                    .atZone(displayZone)
                    )
            );

            raceLine.add(name, start);
            raceList.add(raceLine);
        }

        return raceList;
    }

    private String formatRaceCountdown(
            WeekendCalendarDto weekend
    ) {
        Instant now = Instant.now();

        Instant nextRace = weekend.sessions()
                .stream()
                .filter(WeekendSessionDto::isRace)
                .map(WeekendSessionDto::startsAt)
                .filter(java.util.Objects::nonNull)
                .filter(start -> start.isAfter(now))
                .sorted()
                .findFirst()
                .orElse(null);

        if (nextRace == null) {
            if (weekend.sessions().isEmpty()) {
                return "Time TBA";
            }

            LocalDate today = LocalDate.now(displayZone);

            if (weekend.endDate().isBefore(today)) {
                return "Completed";
            }

            return "Race TBA";
        }

        return formatDuration(
                Duration.between(now, nextRace)
        );
    }

    private String formatDuration(Duration duration) {
        if (duration.isNegative() || duration.isZero()) {
            return "Starting";
        }

        long days = duration.toDays();

        long hours = duration
                .minusDays(days)
                .toHours();

        long minutes = duration
                .minusDays(days)
                .minusHours(hours)
                .toMinutes();

        if (days > 0) {
            return "%dd %dh".formatted(days, hours);
        }

        if (hours > 0) {
            return "%dh %dm".formatted(hours, minutes);
        }

        return "%dm".formatted(minutes);
    }

    /**
     * Phase 5G:
     *
     * This is rendered only after the user expands a weekend.
     */
    private Component createSessionDetails(
            WeekendCalendarDto weekend
    ) {
        VerticalLayout details = new VerticalLayout();

        details.setWidthFull();
        details.setPadding(true);
        details.setSpacing(true);

        details.getStyle()
                .set(
                        "background-color",
                        "var(--lumo-contrast-5pct)"
                )
                .set(
                        "border-radius",
                        "var(--lumo-border-radius-m)"
                );

        if (weekend.sessions().isEmpty()) {
            Paragraph message = new Paragraph(
                    "The detailed timetable has not been "
                    + "published yet."
            );

            message.getStyle()
                    .set(
                            "color",
                            "var(--lumo-secondary-text-color)"
                    );

            details.add(message);
            details.add(createOfficialLink(weekend));

            return details;
        }

        Map<LocalDate, List<WeekendSessionDto>> sessionsByDay = groupSessionsByDay(
                weekend
        );

        for (Map.Entry<
                LocalDate, List<WeekendSessionDto>> day : sessionsByDay.entrySet()) {

            H4 dayHeading = new H4(
                    DAY_HEADING_FORMAT.format(day.getKey())
            );

            dayHeading.getStyle()
                    .set("margin-bottom", "0")
                    .set("margin-top", "var(--lumo-space-m)");

            details.add(dayHeading);

            for (WeekendSessionDto session
                    : day.getValue()) {

                details.add(
                        createSessionLine(session)
                );
            }
        }

        details.add(createOfficialLink(weekend));

        return details;
    }

    private Map<LocalDate, List<WeekendSessionDto>>
            groupSessionsByDay(
                    WeekendCalendarDto weekend
            ) {

        Map<LocalDate, List<WeekendSessionDto>> grouped = new TreeMap<>();

        for (WeekendSessionDto session
                : weekend.sessions()
                        .stream()
                        .filter(item
                                -> item.startsAt() != null
                        )
                        .sorted((first, second)
                                -> first.startsAt().compareTo(
                                second.startsAt()
                        )
                        )
                        .toList()) {

            LocalDate localDate = session.startsAt()
                    .atZone(displayZone)
                    .toLocalDate();

            grouped.computeIfAbsent(
                    localDate,
                    ignored -> new java.util.ArrayList<>()
            ).add(session);
        }

        return grouped;
    }

    private Component createSessionLine(
            WeekendSessionDto session
    ) {
        HorizontalLayout row = new HorizontalLayout();

        row.setWidthFull();
        row.setAlignItems(
                FlexComponent.Alignment.CENTER
        );
        row.setSpacing(true);

        Span time = new Span(
                SESSION_TIME_FORMAT.format(
                        session.startsAt()
                                .atZone(displayZone)
                )
        );

        time.setWidth("65px");

        time.getStyle()
                .set("font-weight", "600")
                .set("font-variant-numeric", "tabular-nums");

        Span name = new Span(session.name());

        if (session.isRace()) {
            name.getStyle()
                    .set("font-weight", "700")
                    .set(
                            "color",
                            "var(--lumo-primary-text-color)"
                    );
        }

        Span status = new Span(
                formatStatus(session)
        );

        status.getStyle()
                .set(
                        "color",
                        "var(--lumo-secondary-text-color)"
                )
                .set("font-size", "var(--lumo-font-size-s)");

        row.add(time, name, status);
        row.expand(name);

        return row;
    }

    private String formatStatus(
            WeekendSessionDto session
    ) {
        return switch (session.status()) {
            case PROVISIONAL ->
                "Provisional";
            case CONFIRMED ->
                "Confirmed";
            case DELAYED ->
                "Delayed";
            case CANCELLED ->
                "Cancelled";
            case COMPLETED ->
                "Completed";
        };
    }

    private Component createOfficialLink(
            WeekendCalendarDto weekend
    ) {
        Div linkContainer = new Div();

        linkContainer.getStyle()
                .set("margin-top", "var(--lumo-space-m)");

        Anchor officialLink = new Anchor(
                weekend.officialUrl(),
                "View official event page"
        );

        officialLink.setTarget("_blank");

        linkContainer.add(
                new Text("Source: "),
                officialLink
        );
        return linkContainer;
    }
}
