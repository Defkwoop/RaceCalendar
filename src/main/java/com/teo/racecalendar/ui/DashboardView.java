package com.teo.racecalendar.ui;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;

import com.teo.racecalendar.service.UpcomingSessionDto;
import com.teo.racecalendar.service.UpcomingSessionService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("Race Calendar")
public class DashboardView extends VerticalLayout {

    private final ZoneId displayZone;

    public DashboardView(
            UpcomingSessionService sessionService,
            @Value("${app.display-zone:Europe/Prague}") String displayZone
    ) {
        this.displayZone = ZoneId.of(displayZone);

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(
                new H1("Race Calendar"),
                new Paragraph(
                        "Upcoming sessions shown in "
                        + this.displayZone
                )
        );

        Grid<UpcomingSessionDto> grid
                = new Grid<>(UpcomingSessionDto.class, false);

        grid.addColumn(UpcomingSessionDto::championship)
                .setHeader("Championship")
                .setAutoWidth(true);

        grid.addColumn(UpcomingSessionDto::meeting)
                .setHeader("Meeting")
                .setAutoWidth(true);

        grid.addColumn(UpcomingSessionDto::session)
                .setHeader("Session")
                .setAutoWidth(true);

        grid.addColumn(this::formatStart)
                .setHeader("Start")
                .setAutoWidth(true);

        grid.addColumn(this::formatCountdown)
                .setHeader("Starts in")
                .setAutoWidth(true);

        grid.addColumn(UpcomingSessionDto::status)
                .setHeader("Status")
                .setAutoWidth(true);

        grid.setItems(sessionService.findUpcoming(50));
        grid.setSizeFull();

        add(grid);
        setFlexGrow(1, grid);
    }

    private String formatStart(UpcomingSessionDto session) {
        DateTimeFormatter formatter
                = DateTimeFormatter.ofPattern(
                        "EEE, d MMM yyyy HH:mm z"
                );

        return formatter.format(
                session.startsAt().atZone(displayZone)
        );
    }

    private String formatCountdown(
            UpcomingSessionDto session
    ) {
        Duration remaining = Duration.between(
                Instant.now(),
                session.startsAt()
        );

        if (remaining.isNegative()) {
            return "Started";
        }

        long days = remaining.toDays();
        long hours = remaining.minusDays(days).toHours();
        long minutes = remaining
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
}
