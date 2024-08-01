package com.justlife.service;

import com.justlife.model.CleaningProfessional;
import com.justlife.repository.CleaningProfessionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CleaningProfessionalService {

    @Autowired
    private CleaningProfessionalRepository cleaningProfessionalRepository;

    public List<CleaningProfessional> getAllCleaningProfessionals() {
        return cleaningProfessionalRepository.findAll();
    }

    public Optional<CleaningProfessional> getCleaningProfessionalById(Long id) {
        return cleaningProfessionalRepository.findById(id);
    }

    public CleaningProfessional createCleaningProfessional(CleaningProfessional cleaningProfessional) {
        return cleaningProfessionalRepository.save(cleaningProfessional);
    }

    public Optional<CleaningProfessional> updateCleaningProfessional(Long id, CleaningProfessional cleaningProfessional) {
        return cleaningProfessionalRepository.findById(id).map(existingProfessional -> {
            cleaningProfessional.setId(id);
            return cleaningProfessionalRepository.save(cleaningProfessional);
        });
    }

    public boolean deleteCleaningProfessional(Long id) {
        return cleaningProfessionalRepository.findById(id).map(professional -> {
            cleaningProfessionalRepository.deleteById(id);
            return true;
        }).orElse(false);
    }
}
