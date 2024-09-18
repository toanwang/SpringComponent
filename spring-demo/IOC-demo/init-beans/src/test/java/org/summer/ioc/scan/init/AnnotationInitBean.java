package org.summer.ioc.scan.init;

import org.summer.ioc.annotation.Component;
import org.summer.ioc.annotation.Value;

import javax.annotation.PostConstruct;

@Component
public class AnnotationInitBean {
    @Value("${app.title}")
    String appTitle;

    @Value("${app.version}")
    String appVersion;

    public String appName;

    @PostConstruct
    void init() {
        this.appName = this.appTitle + " / " + this.appVersion;
    }
}
