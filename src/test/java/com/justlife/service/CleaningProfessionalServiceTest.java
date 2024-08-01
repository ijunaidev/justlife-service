package com.justlife.service;

import com.justlife.model.CleaningProfessional;
import com.justlife.repository.CleaningProfessionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CleaningProfessionalServiceTest {

    @Mock
    private CleaningProfessionalRepository cleaningProfessionalRepository;

    @InjectMocks
    private CleaningProfessionalService cleaningProfessionalService;

    private CleaningProfessional professional;
    private List<CleaningProfessional> professionals;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        professional = new CleaningProfessional();
        professional.setId(1L);
        professional.setName("Alice");

        professionals = new ArrayList<>();
        professionals.add(professional);
    }

    @Test
    void testGetAllCleaningProfessionals() {
        when(cleaningProfessionalRepository.findAll()).thenReturn(professionals);

        List<CleaningProfessional> result = cleaningProfessionalService.getAllCleaningProfessionals();
        assertEquals(1, result.size());
        assertEquals(professional, result.get(0));
    }

    @Test
    void testGetCleaningProfessionalById() {
        when(cleaningProfessionalRepository.findById(1L)).thenReturn(Optional.of(professional));

        Optional<CleaningProfessional> result = cleaningProfessionalService.getCleaningProfessionalById(1L);
        assertTrue(result.isPresent());
        assertEquals(professional, result.get());
    }

    @Test
    void testGetCleaningProfessionalById_NotFound() {
        when(cleaningProfessionalRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<CleaningProfessional> result = cleaningProfessionalService.getCleaningProfessionalById(1L);
        assertFalse(result.isPresent());
    }

    @Test
    void testCreateCleaningProfessional() {
        when(cleaningProfessionalRepository.save(any(CleaningProfessional.class))).thenReturn(professional);

        CleaningProfessional result = cleaningProfessionalService.createCleaningProfessional(professional);
        assertEquals(professional, result);
    }

    @Test
    void testUpdateCleaningProfessional() {
        when(cleaningProfessionalRepository.findById(1L)).thenReturn(Optional.of(professional));
        when(cleaningProfessionalRepository.save(any(CleaningProfessional.class))).thenReturn(professional);

        Optional<CleaningProfessional> result = cleaningProfessionalService.updateCleaningProfessional(1L, professional);
        assertTrue(result.isPresent());
        assertEquals(professional, result.get());
    }

    @Test
    void testUpdateCleaningProfessional_NotFound() {
        when(cleaningProfessionalRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<CleaningProfessional> result = cleaningProfessionalService.updateCleaningProfessional(1L, professional);
        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteCleaningProfessional() {
        when(cleaningProfessionalRepository.findById(1L)).thenReturn(Optional.of(professional));
        doNothing().when(cleaningProfessionalRepository).deleteById(1L);

        boolean result = cleaningProfessionalService.deleteCleaningProfessional(1L);
        assertTrue(result);
    }

    @Test
    void testDeleteCleaningProfessional_NotFound() {
        when(cleaningProfessionalRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = cleaningProfessionalService.deleteCleaningProfessional(1L);
        assertFalse(result);
    }
}
