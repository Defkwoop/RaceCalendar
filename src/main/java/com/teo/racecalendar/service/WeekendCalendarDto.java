package com.teo.racecalendar.service;

import java.time.LocalDate;
import java.util.List;

import com.teo.racecalendar.domain.enums.MeetingFormat;
import com.teo.racecalendar.domain.enums.SeriesCode;

public record WeekendCalendarDto(
        String weekendId,
        SeriesCode series,
        String championship,
        int season,
        Integer roundNumber,
        String eventName,
        String venue,
        String venueTimeZone,
        LocalDate startDate,
        LocalDate endDate,
        MeetingFormat format,
        String officialUrl,
        List<WeekendSessionDto> sessions
        ) {

}
