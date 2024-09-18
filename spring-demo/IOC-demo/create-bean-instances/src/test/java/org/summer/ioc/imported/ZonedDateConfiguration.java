package org.summer.ioc.imported;

import org.summer.ioc.annotation.Bean;
import org.summer.ioc.annotation.Configuration;

import java.time.ZonedDateTime;

@Configuration
public class ZonedDateConfiguration {

    @Bean
    ZonedDateTime startZonedDateTime() {
        return ZonedDateTime.now();
    }
}
