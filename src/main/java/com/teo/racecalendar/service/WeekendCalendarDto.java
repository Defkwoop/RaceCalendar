
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
