package com.justlife.controller;

import com.justlife.model.CleaningProfessional;
import com.justlife.service.CleaningProfessionalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CleaningProfessionalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CleaningProfessionalService cleaningProfessionalService;

    private CleaningProfessional professional;
    private List<CleaningProfessional> professionals;

    @BeforeEach
    void setUp() {
        professional = new CleaningProfessional();
        professional.setId(1L);
        professional.setName("Alice");

        professionals = new ArrayList<>();
        professionals.add(professional);
    }

    @Test
    void testGetAllCleaningProfessionals() throws Exception {
        when(cleaningProfessionalService.getAllCleaningProfessionals()).thenReturn(professionals);

        mockMvc.perform(get("/api/cleaningProfessionals"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[{'id':1,'name':'Alice'}]"));
    }

    @Test
    void testGetCleaningProfessionalById() throws Exception {
        when(cleaningProfessionalService.getCleaningProfessionalById(1L)).thenReturn(Optional.of(professional));

        mockMvc.perform(get("/api/cleaningProfessionals/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{'id':1,'name':'Alice'}"));
    }

    @Test
    void testGetCleaningProfessionalById_NotFound() throws Exception {
        when(cleaningProfessionalService.getCleaningProfessionalById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cleaningProfessionals/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateCleaningProfessional() throws Exception {
        when(cleaningProfessionalService.createCleaningProfessional(any(CleaningProfessional.class))).thenReturn(professional);

        String professionalJson = "{\"name\":\"Alice\"}";

        mockMvc.perform(post("/api/cleaningProfessionals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(professionalJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{'id':1,'name':'Alice'}"));
    }

    @Test
    void testUpdateCleaningProfessional() throws Exception {
        when(cleaningProfessionalService.updateCleaningProfessional(eq(1L), any(CleaningProfessional.class)))
                .thenReturn(Optional.of(professional));

        String professionalJson = "{\"name\":\"Alice\"}";

        mockMvc.perform(put("/api/cleaningProfessionals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(professionalJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{'id':1,'name':'Alice'}"));
    }

    @Test
    void testUpdateCleaningProfessional_NotFound() throws Exception {
        when(cleaningProfessionalService.updateCleaningProfessional(eq(1L), any(CleaningProfessional.class)))
                .thenReturn(Optional.empty());

        String professionalJson = "{\"name\":\"Alice\"}";

        mockMvc.perform(put("/api/cleaningProfessionals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(professionalJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteCleaningProfessional() throws Exception {
        when(cleaningProfessionalService.deleteCleaningProfessional(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/cleaningProfessionals/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteCleaningProfessional_NotFound() throws Exception {
        when(cleaningProfessionalService.deleteCleaningProfessional(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/cleaningProfessionals/1"))
                .andExpect(status().isNotFound());
    }
}
