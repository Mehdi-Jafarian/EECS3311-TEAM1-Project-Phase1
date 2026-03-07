package com.platform.domain;

import java.util.Objects;

/** A consultant who offers services and manages their own time slots. */
public class Consultant {

    private final String id;
    private String name;
    private String email;
    private ConsultantStatus status;

    public Consultant(String id, String name, String email) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.email = Objects.requireNonNull(email, "email");
        this.status = ConsultantStatus.PENDING;
    }

    public String getId()              { return id; }
    public String getName()            { return name; }
    public String getEmail()           { return email; }
    public ConsultantStatus getStatus(){ return status; }

    public boolean isApproved() { return status == ConsultantStatus.APPROVED; }

    public void approve() { this.status = ConsultantStatus.APPROVED; }
    public void reject()  { this.status = ConsultantStatus.REJECTED; }

    @Override
    public String toString() {
        return "Consultant{id='" + id + "', name='" + name + "', status=" + status + "}";
    }
}
