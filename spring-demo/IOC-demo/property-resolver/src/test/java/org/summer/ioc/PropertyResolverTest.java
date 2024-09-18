package org.summer.ioc;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropertyResolverTest {
    @Test
    public void propertyValue() {
        Properties props = new Properties();
        props.setProperty("app.title", "Summer Framework");
        props.setProperty("app.version", "v1.0");
        props.setProperty("jdbc.url", "jdbc:mysql://localhost:3306/simpsons");
        props.setProperty("jdbc.username", "bart");
        props.setProperty("jdbc.password", "51mp50n");
        props.setProperty("jdbc.pool-size", "20");
        props.setProperty("jdbc.auto-commit", "true");
        props.setProperty("scheduler.started-at", "2023-03-29T21:45:01");
        props.setProperty("scheduler.backup-at", "03:05:10");
        props.setProperty("scheduler.cleanup", "P2DT8H21M");

        PropertyResolver pr = new PropertyResolver(props);
        assertEquals("Summer Framework", pr.getProperty("app.title"));
        assertEquals("v1.0", pr.getProperty("app.version"));
        assertEquals("v1.0", pr.getProperty("app.version", "unknown"));
        assertNull(pr.getProperty("app.author"));
        assertEquals("Michael Liao", pr.getProperty("app.author", "Michael Liao"));

        assertTrue(pr.getProperty("jdbc.auto-commit", boolean.class));
        assertEquals(Boolean.TRUE, pr.getProperty("jdbc.auto-commit", Boolean.class));
        assertTrue(pr.getProperty("jdbc.detect-leak", boolean.class, true));

//        assertEquals(20, Optional.ofNullable(pr.getProperty("jdbc.pool-size", int.class)));
//        assertEquals(20, Optional.ofNullable(pr.getProperty("jdbc.pool-size", int.class, 999)));
//        assertEquals(5, Optional.ofNullable(pr.getProperty("jdbc.idle", int.class, 5)));

        assertEquals(LocalDateTime.parse("2023-03-29T21:45:01"), pr.getProperty("scheduler.started-at", LocalDateTime.class));
        assertEquals(LocalTime.parse("03:05:10"), pr.getProperty("scheduler.backup-at", LocalTime.class));
        assertEquals(LocalTime.parse("23:59:59"), pr.getProperty("scheduler.restart-at", LocalTime.class, LocalTime.parse("23:59:59")));
        assertEquals(Duration.ofMinutes((2 * 24 + 8) * 60 + 21), pr.getProperty("scheduler.cleanup", Duration.class));
    }
}
