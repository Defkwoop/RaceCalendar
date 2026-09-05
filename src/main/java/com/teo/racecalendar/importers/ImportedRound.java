package com.teo.racecalendar.importers;

import java.time.LocalDate;
import java.util.List;

import com.teo.racecalendar.domain.enums.MeetingFormat;
import com.teo.racecalendar.domain.enums.RacingCategory;
import com.teo.racecalendar.domain.enums.SeriesCode;

public record ImportedRound(
        SeriesCode seriesCode,
        String championshipName,
        RacingCategory category,
        int season,
        Integer roundNumber,
        String roundName,
        MeetingFormat format,
        String sourceEventId,
        String officialUrl,
        String meetingName,
        LocalDate meetingStart,
        LocalDate meetingEnd,
        ImportedVenue venue,
        List<ImportedSession> sessions
        ) {

}
