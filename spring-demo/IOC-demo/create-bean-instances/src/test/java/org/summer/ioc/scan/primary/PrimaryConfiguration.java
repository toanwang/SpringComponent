package org.summer.ioc.scan.primary;

import org.summer.ioc.annotation.Bean;
import org.summer.ioc.annotation.Configuration;
import org.summer.ioc.annotation.Primary;

@Configuration
public class PrimaryConfiguration {

    @Primary
    @Bean
    DogBean husky() {
        return new DogBean("Husky");
    }

    @Bean
    DogBean teddy() {
        return new DogBean("Teddy");
    }
}
