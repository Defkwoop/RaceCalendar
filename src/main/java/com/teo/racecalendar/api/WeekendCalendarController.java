package com.teo.racecalendar.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.teo.racecalendar.service.WeekendCalendarDto;
import com.teo.racecalendar.service.WeekendCalendarService;

@RestController
@RequestMapping("/api/v1/weekends")
public class WeekendCalendarController {

    private final WeekendCalendarService weekendService;

    public WeekendCalendarController(
            WeekendCalendarService weekendService
    ) {
        this.weekendService = weekendService;
    }

    @GetMapping("/upcoming")
    public List<WeekendCalendarDto> upcoming() {
        return weekendService.findUpcomingWeekends();
    }
}
