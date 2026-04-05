package com.platform.presentation.api;

import com.platform.application.AdminService;
import com.platform.domain.Consultant;
import com.platform.domain.policy.*;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/consultants/pending")
    public List<Consultant> getPendingConsultants() {
        return adminService.getPendingConsultants();
    }

    @PutMapping("/consultants/{id}/approve")
    public void approveConsultant(@PathVariable String id) {
        adminService.approveConsultant(id);
    }

    @PutMapping("/consultants/{id}/reject")
    public void rejectConsultant(@PathVariable String id) {
        adminService.rejectConsultant(id);
    }

    @GetMapping("/policy")
    public Map<String, Object> getSystemPolicy() {
        SystemPolicy policy = adminService.getSystemPolicy();
        return Map.of(
                "cancellationPolicy", policy.getCancellationPolicy().getName(),
                "pricingStrategy", policy.getPricingStrategy().getName(),
                "notificationsEnabled", policy.isNotificationsEnabled()
        );
    }

    @PutMapping("/policy/cancellation")
    public void setCancellationPolicy(@RequestBody Map<String, String> body) {
        String policyName = body.get("policy");
        CancellationPolicy policy = switch (policyName.toUpperCase()) {
            case "FREE" -> new FreeCancellationPolicy();
            case "PARTIAL" -> new PartialRefundPolicy(0.5);
            case "NONE" -> new NoCancellationRefundPolicy();
            default -> throw new IllegalArgumentException("Unknown cancellation policy: " + policyName);
        };
        adminService.setCancellationPolicy(policy);
    }

    @PutMapping("/policy/pricing")
    public void setPricingStrategy(@RequestBody Map<String, String> body) {
        String strategyName = body.get("strategy");
        PricingStrategy strategy = switch (strategyName.toUpperCase()) {
            case "BASE" -> new BasePricingStrategy();
            case "DISCOUNTED" -> new DiscountedPricingStrategy(0.20);
            default -> throw new IllegalArgumentException("Unknown pricing strategy: " + strategyName);
        };
        adminService.setPricingStrategy(strategy);
    }

    @PutMapping("/policy/notifications")
    public void setNotificationsEnabled(@RequestBody Map<String, Object> body) {
        boolean enabled = (Boolean) body.get("enabled");
        adminService.setNotificationsEnabled(enabled);
    }
}
