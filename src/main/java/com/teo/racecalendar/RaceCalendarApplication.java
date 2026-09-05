package com.teo.racecalendar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RaceCalendarApplication {

    public static void main(String[] args) {
        SpringApplication.run(RaceCalendarApplication.class, args);
    }
}
