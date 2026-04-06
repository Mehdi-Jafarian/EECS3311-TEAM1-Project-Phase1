package com.platform.presentation.api;

import com.platform.application.ServiceCatalogService;
import com.platform.domain.ConsultingService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceCatalogService catalogService;

    public ServiceController(ServiceCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<ConsultingService> listServices() {
        return catalogService.listServices();
    }

    @GetMapping("/{id}/price")
    public Map<String, Object> getPrice(@PathVariable String id) {
        double price = catalogService.getPriceFor(id);
        return Map.of("serviceId", id, "price", price);
    }
}
