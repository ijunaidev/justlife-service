package com.justlife.controller;

import com.justlife.model.Booking;
import com.justlife.model.CleaningProfessional;
import com.justlife.service.AvailabilityCheckService;
import com.justlife.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private AvailabilityCheckService availabilityCheckService;

    private Booking booking;
    private CleaningProfessional professional;
    private List<CleaningProfessional> professionals;

    @BeforeEach
    void setUp() {
        professional = new CleaningProfessional();
        professional.setId(1L);
        professional.setName("John Doe");

        professionals = new ArrayList<>();
        professionals.add(professional);

        booking = new Booking();
        booking.setId(1L);
        booking.setStartTime(LocalDateTime.of(2024, 7, 22, 10, 0));
        booking.setDuration(2);
        booking.setProfessionalsRequired(1);
        booking.setProfessionals(professionals);
    }

    @Test
    void testCheckAvailabilityByDate_ValidRequest() throws Exception {
        LocalDate date = LocalDate.of(2024, 7, 22);
        when(availabilityCheckService.checkAvailabilityByDate(any(LocalDate.class), any(Integer.class)))
                .thenReturn(professionals);

        mockMvc.perform(get("/bookings/availability")
                        .param("date", "2024-07-22"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[{'id':1,'name':'John Doe'}]"));
    }

    @Test
    void testCheckAvailabilityByDateTime_ValidRequest() throws Exception {
        LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 0);
        when(availabilityCheckService.checkAvailabilityByDateTime(any(LocalDateTime.class), any(Integer.class), any(Integer.class)))
                .thenReturn(professionals);

        mockMvc.perform(get("/bookings/availability")
                        .param("date", "2024-07-22")
                        .param("startTime", "2024-07-22T10:00:00")
                        .param("duration", "2")
                        .param("professionalsRequired", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[{'id':1,'name':'John Doe'}]"));
    }

    @Test
    void testCheckAvailability_InvalidDateFormat() throws Exception {
        mockMvc.perform(get("/bookings/availability")
                        .param("date", "invalid-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCheckAvailability_InvalidDateTimeFormat() throws Exception {
        mockMvc.perform(get("/bookings/availability")
                        .param("date", "2024-07-22")
                        .param("startTime", "invalid-datetime")
                        .param("duration", "2")
                        .param("professionalsRequired", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateBooking_ValidRequest() throws Exception {
        when(bookingService.createBooking(any(Booking.class))).thenReturn(booking);

        String bookingJson = "{\"startTime\":\"2024-07-22T10:00:00\",\"duration\":2,\"professionalsRequired\":1}";

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{'id':1,'startTime':'2024-07-22T10:00:00','duration':2,'professionalsRequired':1,'professionals':[{'id':1,'name':'John Doe'}]}"));
    }

    @Test
    void testCreateBooking_InvalidRequest_Duration() throws Exception {
        String bookingJson = "{\"startTime\":\"2024-07-22T10:00:00\",\"duration\":3,\"professionalsRequired\":1}";

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateBooking_InvalidRequest_ProfessionalsRequired() throws Exception {
        String bookingJson = "{\"startTime\":\"2024-07-22T10:00:00\",\"duration\":2,\"professionalsRequired\":4}";

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateBooking_ValidRequest() throws Exception {
        when(bookingService.updateBooking(any(Long.class), any(Booking.class))).thenReturn(booking);

        String bookingJson = "{\"startTime\":\"2024-07-22T10:00:00\",\"duration\":2,\"professionalsRequired\":1}";

        mockMvc.perform(put("/bookings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{'id':1,'startTime':'2024-07-22T10:00:00','duration':2,'professionalsRequired':1,'professionals':[{'id':1,'name':'John Doe'}]}"));
    }

    @Test
    void testUpdateBooking_InvalidRequest_Duration() throws Exception {
        String bookingJson = "{\"startTime\":\"2024-07-22T10:00:00\",\"duration\":3,\"professionalsRequired\":1}";

        mockMvc.perform(put("/bookings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateBooking_InvalidRequest_ProfessionalsRequired() throws Exception {
        String bookingJson = "{\"startTime\":\"2024-07-22T10:00:00\",\"duration\":2,\"professionalsRequired\":4}";

        mockMvc.perform(put("/bookings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCheckAvailability_NoProfessionalsRequired() throws Exception {
        LocalDate date = LocalDate.of(2024, 7, 22);
        when(availabilityCheckService.checkAvailabilityByDate(any(LocalDate.class), eq(1)))
                .thenReturn(professionals);

        mockMvc.perform(get("/bookings/availability")
                        .param("date", "2024-07-22"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[{'id':1,'name':'John Doe'}]"));
    }

    @Test
    void testCheckAvailability_ValidDateAndTimeWithoutProfessionalsRequired() throws Exception {
        LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 0);
        when(availabilityCheckService.checkAvailabilityByDateTime(any(LocalDateTime.class), eq(2), eq(1)))
                .thenReturn(professionals);

        mockMvc.perform(get("/bookings/availability")
                        .param("date", "2024-07-22")
                        .param("startTime", "2024-07-22T10:00:00")
                        .param("duration", "2")
                        .param("professionalsRequired", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[{'id':1,'name':'John Doe'}]"));
    }
}
