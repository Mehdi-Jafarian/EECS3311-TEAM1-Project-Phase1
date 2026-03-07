package com.platform.infrastructure.repository;

import com.platform.domain.Consultant;

import java.util.List;
import java.util.Optional;

public interface ConsultantRepository {
    void save(Consultant consultant);
    Optional<Consultant> findById(String id);
    List<Consultant> findAll();
}
