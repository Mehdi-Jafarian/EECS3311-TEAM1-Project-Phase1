package com.platform.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * A discrete time slot owned by a single Consultant.
 * A slot is either available or booked.
 */
public class TimeSlot {

    private final String id;
    private final String consultantId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;

    public TimeSlot(String id, String consultantId,
                    LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.id = Objects.requireNonNull(id, "id");
        this.consultantId = Objects.requireNonNull(consultantId, "consultantId");
        this.date = Objects.requireNonNull(date, "date");
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.endTime = Objects.requireNonNull(endTime, "endTime");
        this.available = true;
    }

    public String    getId()           { return id; }
    public String    getConsultantId() { return consultantId; }
    public LocalDate getDate()         { return date; }
    public LocalTime getStartTime()    { return startTime; }
    public LocalTime getEndTime()      { return endTime; }
    public boolean   isAvailable()     { return available; }

    /** Mark the slot as booked (no longer available). */
    public void book() {
        this.available = false;
    }

    /** Release the slot back to available (e.g., after a cancellation). */
    public void release() {
        this.available = true;
    }

    public void setDate(LocalDate date)         { this.date = date; }
    public void setStartTime(LocalTime startTime){ this.startTime = startTime; }
    public void setEndTime(LocalTime endTime)   { this.endTime = endTime; }

    @Override
    public String toString() {
        return String.format("TimeSlot{id='%s', consultant='%s', date=%s, %s-%s, available=%b}",
                id, consultantId, date, startTime, endTime, available);
    }
}
