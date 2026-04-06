package com.platform.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Abstraction over system time — allows tests to inject a fixed clock
 * without mocking static methods.
 */
public interface TimeProvider {
    LocalDateTime now();
    LocalDate today();
}
