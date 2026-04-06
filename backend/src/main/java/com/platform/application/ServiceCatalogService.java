package com.platform.application;

import com.platform.domain.ConsultingService;
import com.platform.domain.exception.EntityNotFoundException;
import com.platform.domain.policy.SystemPolicy;
import com.platform.infrastructure.repository.ServiceRepository;

import java.util.List;
import java.util.UUID;

/**
 * Application service for UC1 (Browse Consulting Services).
 */
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;
    private final SystemPolicy systemPolicy;

    public ServiceCatalogService(ServiceRepository serviceRepository,
                                  SystemPolicy systemPolicy) {
        this.serviceRepository = serviceRepository;
        this.systemPolicy = systemPolicy;
    }

    /**
     * UC1: List all available consulting services.
     *
     * @return all services in the catalog
     */
    public List<ConsultingService> listServices() {
        return serviceRepository.findAll();
    }

    /**
     * Adds a service to the catalog (admin/setup operation).
     *
     * @param service the service to add
     */
    public void addService(ConsultingService service) {
        serviceRepository.save(service);
    }

    /**
     * Creates and adds a new service to the catalog.
     *
     * @param name            service name
     * @param type            service type
     * @param durationMinutes session duration
     * @param basePrice       base price
     * @return the created {@link ConsultingService}
     */
    public ConsultingService createService(String name, String type,
                                            int durationMinutes, double basePrice) {
        ConsultingService service = new ConsultingService(
                UUID.randomUUID().toString(), name, type, durationMinutes, basePrice);
        serviceRepository.save(service);
        return service;
    }

    /**
     * Returns the effective (post-discount) price for a service using the current pricing strategy.
     *
     * @param serviceId the service ID
     * @return effective price
     * @throws EntityNotFoundException if service not found
     */
    public double getPriceFor(String serviceId) {
        ConsultingService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found: " + serviceId));
        return systemPolicy.getPricingStrategy().calculatePrice(service);
    }
}
