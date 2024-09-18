package org.summer.ioc.scan.init;

import org.summer.ioc.annotation.Component;

@Component
public class SpecifyInitBean {
    String appTitle;

    String appVersion;

    public String appName;

    SpecifyInitBean(String appTitle, String appVersion) {
        this.appTitle = appTitle;
        this.appVersion = appVersion;
    }
    void init() {
        this.appName = this.appTitle + " / " + this.appVersion;
    }

}
