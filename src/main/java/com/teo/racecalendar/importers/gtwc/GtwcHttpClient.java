package com.teo.racecalendar.importers.gtwc;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class GtwcHttpClient {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public String get(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header(
                        "User-Agent",
                        "RaceCalendar/0.1 (+https://github.com/Defkwoop/RaceCalendar)"
                )
                .header(
                        "Accept",
                        "text/html,application/xhtml+xml"
                )
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new GtwcImportException(
                        "GTWC returned HTTP " + response.statusCode()
                        + " for " + url
                );
            }

            return response.body();
        } catch (IOException exception) {
            throw new GtwcImportException(
                    "Could not download GTWC page: " + url,
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new GtwcImportException(
                    "GTWC download was interrupted: " + url,
                    exception
            );
        }
    }
}
