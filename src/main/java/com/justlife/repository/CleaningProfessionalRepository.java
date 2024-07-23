package com.justlife.repository;

import com.justlife.model.CleaningProfessional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CleaningProfessionalRepository extends JpaRepository<CleaningProfessional, Long> {
}