package org.summer.ioc.scan.init;

import org.summer.ioc.annotation.Bean;
import org.summer.ioc.annotation.Configuration;
import org.summer.ioc.annotation.Primary;
import org.summer.ioc.annotation.Value;

@Configuration
public class SpecifyInitConfiguration {
    @Bean(initMethod = "init")
    @Primary
    SpecifyInitBean createSpecifyInitBean(@Value("${app.title}") String appTitle, @Value("${app.version}") String appVersion) {
        return new SpecifyInitBean(appTitle, appVersion);
    }
}
