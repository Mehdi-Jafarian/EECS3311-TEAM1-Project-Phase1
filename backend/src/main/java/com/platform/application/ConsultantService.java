package com.platform.application;

import com.platform.domain.Consultant;
import com.platform.domain.TimeSlot;
import com.platform.domain.exception.ConsultantNotApprovedException;
import com.platform.domain.exception.EntityNotFoundException;
import com.platform.infrastructure.repository.ConsultantRepository;
import com.platform.infrastructure.repository.TimeSlotRepository;

import java.util.List;
import java.util.UUID;

/**
 * Application service for UC8 (Manage Availability) and consultant registration.
 */
public class ConsultantService {

    private final ConsultantRepository consultantRepository;
    private final TimeSlotRepository timeSlotRepository;

    public ConsultantService(ConsultantRepository consultantRepository,
                              TimeSlotRepository timeSlotRepository) {
        this.consultantRepository = consultantRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    /**
     * Registers a new consultant (initially in PENDING status, awaiting admin approval).
     *
     * @param name  the consultant's name
     * @param email the consultant's email
     * @return the created {@link Consultant}
     */
    public Consultant registerConsultant(String name, String email) {
        Consultant consultant = new Consultant(UUID.randomUUID().toString(), name, email);
        consultantRepository.save(consultant);
        return consultant;
    }

    /**
     * Retrieves a consultant by ID.
     *
     * @throws EntityNotFoundException if not found
     */
    public Consultant getConsultant(String id) {
        return consultantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Consultant not found: " + id));
    }

    /**
     * Returns all registered consultants.
     */
    public List<Consultant> getAllConsultants() {
        return consultantRepository.findAll();
    }

    // ── UC8 ──────────────────────────────────────────────────────────────────

    /**
     * UC8: Consultant adds an available time slot.
     *
     * @throws ConsultantNotApprovedException if the consultant is not yet approved
     */
    public TimeSlot addTimeSlot(String consultantId, TimeSlot slot) {
        Consultant consultant = getConsultant(consultantId);
        if (!consultant.isApproved()) {
            throw new ConsultantNotApprovedException(consultantId);
        }
        timeSlotRepository.save(slot);
        return slot;
    }

    /**
     * UC8: Get all time slots for a consultant.
     */
    public List<TimeSlot> getTimeSlots(String consultantId) {
        return timeSlotRepository.findByConsultantId(consultantId);
    }

    /**
     * UC8: Update an existing time slot.
     */
    public void updateTimeSlot(TimeSlot slot) {
        timeSlotRepository.findById(slot.getId())
                .orElseThrow(() -> new EntityNotFoundException("TimeSlot not found: " + slot.getId()));
        timeSlotRepository.save(slot);
    }
}
