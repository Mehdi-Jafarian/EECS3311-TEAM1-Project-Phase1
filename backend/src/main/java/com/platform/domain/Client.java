package com.platform.domain;

import java.util.Objects;

/** A registered client who can browse services, make bookings, and process payments. */
public class Client {

    private final String id;
    private String name;
    private String email;

    public Client(String id, String name, String email) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.email = Objects.requireNonNull(email, "email");
    }

    public String getId()    { return id; }
    public String getName()  { return name; }
    public String getEmail() { return email; }

    public void setName(String name)   { this.name = name; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "Client{id='" + id + "', name='" + name + "', email='" + email + "'}";
    }
}
