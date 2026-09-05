package com.teo.racecalendar.service;

import java.time.Year;
import java.time.ZoneOffset;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.teo.racecalendar.importers.CalendarProvider;

@Component
public class CalendarSyncScheduler {

    private static final Logger log
            = LoggerFactory.getLogger(CalendarSyncScheduler.class);

    private final List<CalendarProvider> providers;
    private final CalendarImportService importService;
    private final boolean enabled;

    public CalendarSyncScheduler(
            List<CalendarProvider> providers,
            CalendarImportService importService,
            @Value("${app.sync.enabled:true}") boolean enabled
    ) {
        this.providers = providers;
        this.importService = importService;
        this.enabled = enabled;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void importOnStartup() {
        synchronize();
    }

    @Scheduled(
            fixedDelayString = "${app.sync.interval:PT6H}",
            initialDelayString = "PT1M"
    )
    public void synchronize() {
        if (!enabled) {
            return;
        }

        int currentYear = Year.now(ZoneOffset.UTC).getValue();

        for (CalendarProvider provider : providers) {
            synchronizeProvider(provider, currentYear);
            synchronizeProvider(provider, currentYear + 1);
        }
    }

    private void synchronizeProvider(
            CalendarProvider provider,
            int season
    ) {
        try {
            log.info(
                    "Synchronizing provider={} season={}",
                    provider.providerId(),
                    season
            );

            importService.importSeason(provider, season);
        } catch (Exception exception) {
            log.error(
                    "Calendar synchronization failed for provider={} season={}",
                    provider.providerId(),
                    season,
                    exception
            );
        }
    }
}
