package com.platform.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Time provider that always returns a fixed instant.
 * Useful for deterministic unit tests.
 */
public class FixedTimeProvider implements TimeProvider {

    private LocalDateTime fixedTime;

    public FixedTimeProvider(LocalDateTime fixedTime) {
        this.fixedTime = fixedTime;
    }

    public void setFixedTime(LocalDateTime fixedTime) {
        this.fixedTime = fixedTime;
    }

    @Override
    public LocalDateTime now() {
        return fixedTime;
    }

    @Override
    public LocalDate today() {
        return fixedTime.toLocalDate();
    }
}
