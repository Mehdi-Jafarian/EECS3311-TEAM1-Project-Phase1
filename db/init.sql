-- Database schema for Service Booking & Consulting Platform

CREATE TABLE IF NOT EXISTS clients (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS consultants (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);

CREATE TABLE IF NOT EXISTS services (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(100) NOT NULL,
    duration_minutes INT NOT NULL,
    base_price DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS time_slots (
    id VARCHAR(36) PRIMARY KEY,
    consultant_id VARCHAR(36) NOT NULL REFERENCES consultants(id),
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS bookings (
    id VARCHAR(36) PRIMARY KEY,
    client_id VARCHAR(36) NOT NULL REFERENCES clients(id),
    consultant_id VARCHAR(36) NOT NULL REFERENCES consultants(id),
    service_id VARCHAR(36) NOT NULL REFERENCES services(id),
    time_slot_id VARCHAR(36) NOT NULL REFERENCES time_slots(id),
    status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED',
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS payments (
    id VARCHAR(36) PRIMARY KEY,
    booking_id VARCHAR(36) NOT NULL REFERENCES bookings(id),
    client_id VARCHAR(36) NOT NULL REFERENCES clients(id),
    amount DOUBLE PRECISION NOT NULL,
    payment_type VARCHAR(30) NOT NULL,
    transaction_id VARCHAR(100) NOT NULL,
    paid_at TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SUCCESS'
);

CREATE TABLE IF NOT EXISTS payment_methods (
    id VARCHAR(36) PRIMARY KEY,
    client_id VARCHAR(36) NOT NULL REFERENCES clients(id),
    payment_type VARCHAR(30) NOT NULL,
    card_number VARCHAR(16),
    expiry_month INT,
    expiry_year INT,
    cvv VARCHAR(4),
    email VARCHAR(255),
    account_number VARCHAR(17),
    routing_number VARCHAR(9)
);

CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(36) PRIMARY KEY,
    recipient_id VARCHAR(36) NOT NULL,
    message TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL
);

-- Seed demo data
INSERT INTO services (id, name, type, duration_minutes, base_price) VALUES
    ('svc-001', 'Career Coaching', 'Coaching', 60, 150.0),
    ('svc-002', 'Tech Architecture Review', 'Technical', 90, 250.0),
    ('svc-003', 'Business Strategy', 'Strategy', 120, 300.0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO clients (id, name, email) VALUES
    ('cli-001', 'Alice Johnson', 'alice@example.com'),
    ('cli-002', 'Bob Smith', 'bob@example.com')
ON CONFLICT (id) DO NOTHING;

INSERT INTO consultants (id, name, email, status) VALUES
    ('con-001', 'Dr. Carol White', 'carol@example.com', 'APPROVED')
ON CONFLICT (id) DO NOTHING;

INSERT INTO time_slots (id, consultant_id, date, start_time, end_time, available) VALUES
    ('slot-001', 'con-001', CURRENT_DATE + INTERVAL '1 day', '09:00:00', '10:00:00', TRUE),
    ('slot-002', 'con-001', CURRENT_DATE + INTERVAL '1 day', '11:00:00', '12:00:00', TRUE)
ON CONFLICT (id) DO NOTHING;
