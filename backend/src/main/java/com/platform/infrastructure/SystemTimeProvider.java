package com.platform.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Uses the real system clock. */
public class SystemTimeProvider implements TimeProvider {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    @Override
    public LocalDate today() {
        return LocalDate.now();
    }
}
