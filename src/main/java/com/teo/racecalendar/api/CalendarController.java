package com.teo.racecalendar.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.teo.racecalendar.service.UpcomingSessionDto;
import com.teo.racecalendar.service.UpcomingSessionService;

@RestController
@RequestMapping("/api/v1/sessions")
public class CalendarController {

    private final UpcomingSessionService sessionService;

    public CalendarController(
            UpcomingSessionService sessionService
    ) {
        this.sessionService = sessionService;
    }

    @GetMapping("/upcoming")
    public List<UpcomingSessionDto> upcoming(
            @RequestParam(defaultValue = "25") int limit
    ) {
        return sessionService.findUpcoming(limit);
    }
}
