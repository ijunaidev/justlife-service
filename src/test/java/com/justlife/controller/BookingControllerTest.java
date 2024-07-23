package com.justlife.controller;

import com.justlife.model.CleaningProfessional;
import com.justlife.service.AvailabilityCheckService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class BookingControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private AvailabilityCheckService availabilityCheckService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testCheckAvailability_InvalidDateFormat() throws Exception {
        mockMvc.perform(get("/bookings/availability")
                        .param("startTime", "invalid-date")
                        .param("duration", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCheckAvailability() throws Exception {
        List<CleaningProfessional> professionals = new ArrayList<>();
        when(availabilityCheckService.checkAvailability(any(LocalDateTime.class), any(Integer.class)))
                .thenReturn(professionals);

        mockMvc.perform(get("/bookings/availability")
                        .param("startTime", "2024-07-22T10:00:00")
                        .param("duration", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void testCheckAvailability_DurationExceedsWorkingHours() throws Exception {
        List<CleaningProfessional> professionals = new ArrayList<>();
        when(availabilityCheckService.checkAvailability(any(LocalDateTime.class), any(Integer.class)))
                .thenReturn(professionals);

        mockMvc.perform(get("/bookings/availability")
                        .param("startTime", "2024-07-22T17:00:00")
                        .param("duration", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void testCheckAvailability_shouldReturnBadRequestForMissingStartTime() throws Exception {
        mockMvc.perform(get("/bookings/availability")
                        .param("duration", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCheckAvailability_shouldReturnBadRequestForMissingDuration() throws Exception {
        mockMvc.perform(get("/bookings/availability")
                        .param("startTime", "2024-07-22T10:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCheckAvailability_shouldReturnListOfProfessionalsForValidRequest() throws Exception {
        CleaningProfessional professional = new CleaningProfessional();
        professional.setId(1L);
        professional.setName("John Doe");
        professional.setAvailable(true);

        List<CleaningProfessional> professionals = new ArrayList<>();
        professionals.add(professional);
        when(availabilityCheckService.checkAvailability(any(LocalDateTime.class), any(Integer.class)))
                .thenReturn(professionals);

        mockMvc.perform(get("/bookings/availability")
                        .param("startTime", "2024-07-22T10:00:00")
                        .param("duration", "2"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{'id':1,'name':'John Doe','available':true}]"));
    }

    @Test
    void testCheckAvailability_shouldReturnBadRequestForNonIntegerDuration() throws Exception {
        mockMvc.perform(get("/bookings/availability")
                        .param("startTime", "2024-07-22T10:00:00")
                        .param("duration", "two"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCheckAvailability_shouldReturnBadRequestForPastStartTime() throws Exception {
        mockMvc.perform(get("/bookings/availability")
                        .param("startTime", "2020-07-22T10:00:00")
                        .param("duration", "2"))
                .andExpect(status().isBadRequest());
    }
}
