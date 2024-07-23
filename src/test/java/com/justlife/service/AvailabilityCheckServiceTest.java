package com.justlife.service;

import com.justlife.model.Booking;
import com.justlife.model.CleaningProfessional;
import com.justlife.repository.BookingRepository;
import com.justlife.repository.CleaningProfessionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class AvailabilityCheckServiceTest {

    @Autowired
    private AvailabilityCheckService availabilityCheckService;

    @MockBean
    private CleaningProfessionalRepository professionalRepository;

    @MockBean
    private BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {

    }


    @Test
    void testCheckAvailability() {
        CleaningProfessional professional1 = new CleaningProfessional();
        professional1.setId(1L);
        professional1.setName("John");
        professional1.setWorkingHours("08:00-22:00");
        professional1.setWorkingOnFridays(false);

        CleaningProfessional professional2 = new CleaningProfessional();
        professional2.setId(2L);
        professional2.setName("Jane");
        professional2.setWorkingHours("08:00-22:00");
        professional2.setWorkingOnFridays(false);

        List<CleaningProfessional> professionals = Arrays.asList(professional1, professional2);
        when(professionalRepository.findAll()).thenReturn(professionals);

        LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 0);
        int duration = 2;

        List<Booking> conflictingBookings = new ArrayList<>();
        when(bookingRepository.findByStartTimeLessThanEqualAndEndTimeGreaterThanEqual(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(conflictingBookings);

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration);

        assertEquals(2, availableProfessionals.size());
    }

    @Test
    void testCheckAvailability_DurationExceedsWorkingHours() {
        CleaningProfessional professional = new CleaningProfessional();
        professional.setId(1L);
        professional.setName("John");
        professional.setWorkingHours("08:00-18:00");
        professional.setWorkingOnFridays(false);

        when(professionalRepository.findAll()).thenReturn(Arrays.asList(professional));

        LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 17, 0);
        int duration = 2;

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailability_NonWorkingDay() {
        CleaningProfessional professional = new CleaningProfessional();
        professional.setId(1L);
        professional.setName("John");
        professional.setWorkingHours("08:00-22:00");
        professional.setWorkingOnFridays(false);

        when(professionalRepository.findAll()).thenReturn(Arrays.asList(professional));

        // Friday
        LocalDateTime startTime = LocalDateTime.of(2024, 7, 26, 10, 0);
        int duration = 2;

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration);

        assertEquals(0, availableProfessionals.size());
    }


    @Test
    void testCheckAvailability_availabilityDuringNonWorkingHoursShouldReturnEmptyList() {
        LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 6, 0); // Before working hours
        int duration = 2;

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailability_availabilityDuringWorkingHoursShouldReturnProfessionals() {
        LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 0); // Within working hours
        int duration = 2;
        CleaningProfessional professional1 = new CleaningProfessional();
        professional1.setId(1L);
        professional1.setName("John");
        professional1.setWorkingHours("08:00-22:00");
        professional1.setWorkingOnFridays(true);
        List<CleaningProfessional> professionals = Arrays.asList(professional1);

        when(professionalRepository.findAll()).thenReturn(professionals);
        when(bookingRepository.findByStartTimeLessThanEqualAndEndTimeGreaterThanEqual(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration);

        assertEquals(1, availableProfessionals.size());
        assertEquals(professional1.getId(), availableProfessionals.get(0).getId());
    }


    @Test
    void testCheckAvailability_availabilityWithConflictingBookingsShouldReturnEmptyList() {
        LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 0);
        int duration = 2;

        List<Booking> conflictingBookings = new ArrayList<>();
        Booking booking = new Booking();
        booking.setStartTime(startTime);
        booking.setEndTime(startTime.plusHours(duration));
        conflictingBookings.add(booking);

        when(bookingRepository.findByStartTimeLessThanEqualAndEndTimeGreaterThanEqual(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(conflictingBookings);

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailability_availabilityOnNonWorkingDayShouldReturnEmptyList() {
        LocalDateTime startTime = LocalDateTime.of(2024, 7, 26, 10, 0); // Friday, assuming professionals do not work on Fridays
        int duration = 2;

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailability_availabilityForExtendedDurationBeyondWorkingHoursShouldReturnEmptyList() {
        LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 20, 0); // 2 hours before the end of working hours
        int duration = 3; // Extends beyond working hours

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration);

        assertEquals(0, availableProfessionals.size());
    }


    @Test
    void availabilityWithSufficientBreakShouldReturnProfessionals() {
        LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 11, 0); // Assuming a booking exists ending at 10:30
        int duration = 1;

        CleaningProfessional professional = new CleaningProfessional();
        professional.setId(1L);
        professional.setName("Available Professional");
        professional.setWorkingHours("08:00-22:00");
        professional.setWorkingOnFridays(true);

        List<Booking> existingBookings = new ArrayList<>();
        Booking existingBooking = new Booking();
        existingBooking.setEndTime(startTime.minusMinutes(30)); // Ends at 10:30
        existingBookings.add(existingBooking);

        when(professionalRepository.findAll()).thenReturn(Arrays.asList(professional));
        when(bookingRepository.findByStartTimeLessThanEqualAndEndTimeGreaterThanEqual(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(existingBookings);

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration);

        assertFalse(availableProfessionals.isEmpty());
    }

    @Test
    void availabilityWithoutSufficientBreakShouldReturnEmptyList() {
        LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 45); // New booking start time
        int duration = 1;

        CleaningProfessional professional = new CleaningProfessional();
        professional.setId(1L);
        professional.setName("Unavailable Professional");
        professional.setWorkingHours("08:00-22:00");
        professional.setWorkingOnFridays(true);

        Booking conflictingBooking = new Booking();
        conflictingBooking.setStartTime(startTime.minusHours(1)); // Example start time 1 hour before the desired start time
        conflictingBooking.setEndTime(startTime.plusMinutes(15)); // This end time conflicts with the desired start time minus 30 minutes
        conflictingBooking.setProfessionals(Arrays.asList(professional));

        List<Booking> existingBookings = new ArrayList<>();
        existingBookings.add(conflictingBooking);

        when(professionalRepository.findAll()).thenReturn(Arrays.asList(professional));
        when(bookingRepository.findByStartTimeLessThanEqualAndEndTimeGreaterThanEqual(startTime.plusHours(duration).plusMinutes(30), startTime.minusMinutes(30)))
                .thenReturn(existingBookings);

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration);

        assertTrue(availableProfessionals.isEmpty());
    }

}
