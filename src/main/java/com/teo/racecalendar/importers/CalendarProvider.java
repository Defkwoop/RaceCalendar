package com.teo.racecalendar.importers;

import java.util.List;

public interface CalendarProvider {

    String providerId();

    List<ImportedRound> fetchSeason(int season);
}
