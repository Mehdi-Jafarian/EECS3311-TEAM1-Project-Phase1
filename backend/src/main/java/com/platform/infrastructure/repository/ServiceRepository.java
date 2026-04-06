package com.platform.infrastructure.repository;

import com.platform.domain.ConsultingService;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository {
    void save(ConsultingService service);
    Optional<ConsultingService> findById(String id);
    List<ConsultingService> findAll();
}
