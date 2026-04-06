package com.platform.domain;

import java.util.Objects;

/** A consulting service offered on the platform (e.g., Career Coaching, Tech Review). */
public class ConsultingService {

    private final String id;
    private String name;
    private String type;
    private int durationMinutes;
    private double basePrice;

    public ConsultingService(String id, String name, String type,
                             int durationMinutes, double basePrice) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");
        this.durationMinutes = durationMinutes;
        this.basePrice = basePrice;
    }

    public String getId()             { return id; }
    public String getName()           { return name; }
    public String getType()           { return type; }
    public int    getDurationMinutes(){ return durationMinutes; }
    public double getBasePrice()      { return basePrice; }

    public void setName(String name)                  { this.name = name; }
    public void setType(String type)                  { this.type = type; }
    public void setDurationMinutes(int durationMinutes){ this.durationMinutes = durationMinutes; }
    public void setBasePrice(double basePrice)         { this.basePrice = basePrice; }

    @Override
    public String toString() {
        return String.format("ConsultingService{id='%s', name='%s', type='%s', duration=%dmin, price=%.2f}",
                id, name, type, durationMinutes, basePrice);
    }
}
