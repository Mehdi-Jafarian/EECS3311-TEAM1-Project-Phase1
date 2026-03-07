package com.platform.application;

import com.platform.domain.Consultant;
import com.platform.domain.exception.EntityNotFoundException;
import com.platform.domain.policy.CancellationPolicy;
import com.platform.domain.policy.PricingStrategy;
import com.platform.domain.policy.SystemPolicy;
import com.platform.infrastructure.repository.ConsultantRepository;

import java.util.List;

/**
 * Application service for UC11 (Approve Consultant Registration) and UC12 (Define System Policies).
 */
public class AdminService {

    private final ConsultantRepository consultantRepository;
    private final SystemPolicy systemPolicy;
    private final NotificationService notificationService;

    public AdminService(ConsultantRepository consultantRepository,
                        SystemPolicy systemPolicy,
                        NotificationService notificationService) {
        this.consultantRepository = consultantRepository;
        this.systemPolicy = systemPolicy;
        this.notificationService = notificationService;
    }

    // ── UC11 ─────────────────────────────────────────────────────────────────

    /**
     * UC11: Approve a consultant registration.
     *
     * @param consultantId the consultant to approve
     * @throws EntityNotFoundException if consultant not found
     */
    public void approveConsultant(String consultantId) {
        Consultant consultant = getConsultantOrThrow(consultantId);
        consultant.approve();
        consultantRepository.save(consultant);
        notificationService.send(consultantId,
                "Congratulations! Your consultant registration has been approved.");
    }

    /**
     * UC11: Reject a consultant registration.
     *
     * @param consultantId the consultant to reject
     * @throws EntityNotFoundException if consultant not found
     */
    public void rejectConsultant(String consultantId) {
        Consultant consultant = getConsultantOrThrow(consultantId);
        consultant.reject();
        consultantRepository.save(consultant);
        notificationService.send(consultantId,
                "Your consultant registration has been rejected.");
    }

    /**
     * Returns all pending consultants awaiting admin review.
     */
    public List<Consultant> getPendingConsultants() {
        return consultantRepository.findAll().stream()
                .filter(c -> c.getStatus() == com.platform.domain.ConsultantStatus.PENDING)
                .toList();
    }

    /**
     * Returns all consultants.
     */
    public List<Consultant> getAllConsultants() {
        return consultantRepository.findAll();
    }

    // ── UC12 ─────────────────────────────────────────────────────────────────

    /**
     * UC12: Set the active cancellation policy.
     *
     * @param policy the new cancellation policy
     */
    public void setCancellationPolicy(CancellationPolicy policy) {
        systemPolicy.setCancellationPolicy(policy);
        System.out.println("[POLICY] Cancellation policy changed to: " + policy.getName());
    }

    /**
     * UC12: Set the active pricing strategy.
     *
     * @param strategy the new pricing strategy
     */
    public void setPricingStrategy(PricingStrategy strategy) {
        systemPolicy.setPricingStrategy(strategy);
        System.out.println("[POLICY] Pricing strategy changed to: " + strategy.getName());
    }

    /**
     * UC12: Enable or disable the notification system.
     *
     * @param enabled whether notifications should be sent
     */
    public void setNotificationsEnabled(boolean enabled) {
        systemPolicy.setNotificationsEnabled(enabled);
        System.out.println("[POLICY] Notifications " + (enabled ? "enabled" : "disabled") + ".");
    }

    /**
     * Returns a snapshot of the current system policy settings.
     */
    public SystemPolicy getSystemPolicy() {
        return systemPolicy;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Consultant getConsultantOrThrow(String consultantId) {
        return consultantRepository.findById(consultantId)
                .orElseThrow(() -> new EntityNotFoundException("Consultant not found: " + consultantId));
    }
}
