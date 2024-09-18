package org.summer.ioc.scan;

import org.summer.ioc.annotation.Component;
import org.summer.ioc.annotation.Import;
import org.summer.ioc.imported.LocalDateConfiguration;
import org.summer.ioc.imported.ZonedDateConfiguration;

@Component
@Import({ LocalDateConfiguration.class, ZonedDateConfiguration.class })
public class ScanApplication {
}
