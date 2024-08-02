package com.justlife.service;

import com.justlife.model.Booking;
import com.justlife.model.BookingDetail;
import com.justlife.model.CleaningProfessional;
import com.justlife.repository.BookingDetailRepository;
import com.justlife.repository.BookingRepository;
import com.justlife.repository.CleaningProfessionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AvailabilityCheckServiceTest {

    @Mock
    private CleaningProfessionalRepository professionalRepository;

    @Mock
    private BookingDetailRepository bookingDetailRepository;

    @InjectMocks
    private AvailabilityCheckService availabilityCheckService;

    private List<CleaningProfessional> professionals;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        professionals = new ArrayList<>();
        CleaningProfessional professional1 = new CleaningProfessional();
        professional1.setId(1L);
        professional1.setName("John Doe");
        professional1.setWorkingHours("08:00-22:00");
        professional1.setWorkingOnFridays(false);

        CleaningProfessional professional2 = new CleaningProfessional();
        professional2.setId(2L);
        professional2.setName("Jane Doe");
        professional2.setWorkingHours("08:00-22:00");
        professional2.setWorkingOnFridays(false);

        professionals.add(professional1);
        professionals.add(professional2);
    }

    @Test
    void testCheckAvailabilityByDate_ProfessionalsAvailable() {
        when(professionalRepository.findAll()).thenReturn(professionals);
        when(bookingDetailRepository.findByCleaningProfessionalIdAndBookingStartTimeBetween(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        LocalDate date = LocalDate.of(2024, 7, 22);
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDate(date, 2);

        assertEquals(2, availableProfessionals.size());
    }

    @Test
    void testCheckAvailabilityByDate_NoProfessionalsAvailable() {
        when(professionalRepository.findAll()).thenReturn(new ArrayList<>());

        LocalDate date = LocalDate.of(2024, 7, 22);
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDate(date, 1);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailabilityByDateTime_ProfessionalsAvailable() {
        when(professionalRepository.findAll()).thenReturn(professionals);
        when(bookingDetailRepository.findByCleaningProfessionalIdAndBookingStartTimeBetween(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 0);
        int duration = 2;
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDateTime(startTime, duration, 2);

        assertEquals(2, availableProfessionals.size());
    }

    @Test
    void testCheckAvailabilityByDateTime_NoProfessionalsAvailable() {
        when(professionalRepository.findAll()).thenReturn(new ArrayList<>());

        LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 0);
        int duration = 2;
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDateTime(startTime, duration, 1);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailabilityByDateTime_ProfessionalsUnavailableDueToWorkingHours() {
        when(professionalRepository.findAll()).thenReturn(professionals);

        LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 7, 0); // Before working hours
        int duration = 2;
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDateTime(startTime, duration, 1);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailabilityByDateTime_ProfessionalsUnavailableDueToEndWorkingHours() {
        when(professionalRepository.findAll()).thenReturn(professionals);

        LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 21, 0); // Ending after working hours
        int duration = 2;
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDateTime(startTime, duration, 1);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailabilityByDateTime_ProfessionalNotAvailableOnFriday() {
        when(professionalRepository.findAll()).thenReturn(professionals);

        LocalDateTime startTime = LocalDateTime.of(2024, 7, 26, 10, 0); // Friday
        int duration = 2;
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDateTime(startTime, duration, 1);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailabilityByDateTime_ProfessionalAvailableWithSpecificDuration() {
        when(professionalRepository.findAll()).thenReturn(professionals);
        when(bookingDetailRepository.findByCleaningProfessionalIdAndBookingStartTimeBetween(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 0);
        int duration = 4;
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDateTime(startTime, duration, 2);

        assertEquals(2, availableProfessionals.size());
    }

    @Test
    void testCheckAvailabilityByDateTime_ProfessionalUnavailableWithSpecificDuration() {
        when(professionalRepository.findAll()).thenReturn(professionals);

        Booking booking = new Booking();
        booking.setStartTime(LocalDateTime.of(2024, 7, 22, 8, 0));
        booking.setEndTime(LocalDateTime.of(2024, 7, 22, 12, 0)); // Overlaps with the requested duration

        BookingDetail bookingDetail = new BookingDetail();
        bookingDetail.setBooking(booking);
        bookingDetail.setCleaningProfessional(professionals.get(0));

        List<BookingDetail> bookingDetails = new ArrayList<>();
        bookingDetails.add(bookingDetail);

        when(bookingDetailRepository.findByCleaningProfessionalIdAndBookingStartTimeBetween(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(bookingDetails);

        LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 0);
        int duration = 4;
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDateTime(startTime, duration, 2);

        assertEquals(0, availableProfessionals.size());
    }
}
