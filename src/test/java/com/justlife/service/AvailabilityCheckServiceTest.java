package com.justlife.service;

import com.justlife.model.Booking;
import com.justlife.model.CleaningProfessional;
import com.justlife.model.Vehicle;
import com.justlife.repository.BookingRepository;
import com.justlife.repository.CleaningProfessionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private List<Vehicle> vehicles;
    private List<CleaningProfessional> professionals;

    @BeforeEach
    void setUp() {
        vehicles = new ArrayList<>();
        professionals = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            final int vehicleIndex = i;
            Vehicle vehicle = new Vehicle();
            vehicle.setId((long) vehicleIndex);
            vehicle.setName("Vehicle " + vehicleIndex);

            List<CleaningProfessional> vehicleProfessionals = IntStream.range(1, 6)
                    .mapToObj(j -> {
                        CleaningProfessional professional = new CleaningProfessional();
                        professional.setId((long) ((vehicleIndex - 1) * 5 + j));
                        professional.setName("Professional " + ((vehicleIndex - 1) * 5 + j));
                        professional.setWorkingHours("08:00-22:00");
                        professional.setWorkingOnFridays(false);
                        professional.setVehicle(vehicle);
                        return professional;
                    }).collect(Collectors.toList());

            vehicle.setProfessionals(vehicleProfessionals);
            vehicles.add(vehicle);
            professionals.addAll(vehicleProfessionals);
        }
    }

    // Test cases

    @Test
    void testCheckAvailability_ProfessionalAvailable() {
        when(professionalRepository.findAll()).thenReturn(professionals);
        when(bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        final LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 0);
        final int duration = 2;

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration, 1);

        assertEquals(25, availableProfessionals.size());
    }

    @Test
    void testCheckAvailability_ProfessionalNotAvailableOnFriday() {
        when(professionalRepository.findAll()).thenReturn(professionals);

        final LocalDateTime startTime = LocalDateTime.of(2024, 7, 26, 10, 0); // Friday
        final int duration = 2;

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration, 1);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailability_ProfessionalNotAvailableOutsideWorkingHours() {
        when(professionalRepository.findAll()).thenReturn(professionals);

        final LocalDateTime startTimeBeforeHours = LocalDateTime.of(2024, 7, 22, 7, 0); // Before working hours
        final int duration = 2;

        List<CleaningProfessional> availableProfessionalsBeforeHours = availabilityCheckService.checkAvailability(startTimeBeforeHours, duration, 1);

        assertEquals(0, availableProfessionalsBeforeHours.size());

        final LocalDateTime startTimeAfterHours = LocalDateTime.of(2024, 7, 22, 21, 0); // Ending after working hours

        List<CleaningProfessional> availableProfessionalsAfterHours = availabilityCheckService.checkAvailability(startTimeAfterHours, duration, 1);

        assertEquals(0, availableProfessionalsAfterHours.size());
    }

    @Test
    void testCheckAvailability_ProfessionalBookedWithoutBreak() {
        when(professionalRepository.findAll()).thenReturn(professionals);

        Booking booking = new Booking();
        booking.setStartTime(LocalDateTime.of(2024, 7, 22, 8, 0));
        booking.setEndTime(LocalDateTime.of(2024, 7, 22, 10, 0));

        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);

        when(bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(bookings);

        final LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 0); // Directly after existing booking
        final int duration = 2;

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration, 1);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailability_ProfessionalBookedWithBreak() {
        when(professionalRepository.findAll()).thenReturn(professionals);

        Booking booking = new Booking();
        booking.setStartTime(LocalDateTime.of(2024, 7, 22, 8, 0));
        booking.setEndTime(LocalDateTime.of(2024, 7, 22, 10, 0));

        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);

        when(bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(bookings);

        final LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 31); // 30 minutes after existing booking
        final int duration = 2;

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration, 1);

        assertEquals(25, availableProfessionals.size());
    }

    @Test
    void testCheckAvailability_MultipleProfessionalsAvailable() {
        when(professionalRepository.findAll()).thenReturn(professionals);
        when(bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        final LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 0);
        final int duration = 2;

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration, 3);

        assertEquals(25, availableProfessionals.size());
    }

    @Test
    void testCheckAvailability_MultipleProfessionalsUnavailableDueToBookings() {
        when(professionalRepository.findAll()).thenReturn(professionals);

        Booking booking1 = new Booking();
        booking1.setStartTime(LocalDateTime.of(2024, 7, 22, 8, 0));
        booking1.setEndTime(LocalDateTime.of(2024, 7, 22, 10, 0));

        Booking booking2 = new Booking();
        booking2.setStartTime(LocalDateTime.of(2024, 7, 22, 8, 0));
        booking2.setEndTime(LocalDateTime.of(2024, 7, 22, 10, 0));

        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking1);
        bookings.add(booking2);

        when(bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(bookings);

        final LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 0);
        final int duration = 2;

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration, 3);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailability_ProfessionalAvailableButNotEnoughProfessionals() {
        when(professionalRepository.findAll()).thenReturn(professionals);
        when(bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        final LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 0);
        final int duration = 2;

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration, 26);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailability_ProfessionalAvailableWithSomeUnavailableDueToBookings() {
        when(professionalRepository.findAll()).thenReturn(professionals);

        Booking booking = new Booking();
        booking.setStartTime(LocalDateTime.of(2024, 7, 22, 8, 0));
        booking.setEndTime(LocalDateTime.of(2024, 7, 22, 10, 0));

        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);

        when(bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(bookings);

        final LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 31); // 30 minutes after existing booking
        final int duration = 2;

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration, 5);

        assertEquals(25, availableProfessionals.size());
    }

    @Test
    void testCheckAvailability_ProfessionalAvailableWithSpecificDuration() {
        when(professionalRepository.findAll()).thenReturn(professionals);
        when(bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        final LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 0);
        final int duration = 4;

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration, 2);

        assertEquals(25, availableProfessionals.size());
    }

    @Test
    void testCheckAvailability_ProfessionalUnavailableWithSpecificDuration() {
        when(professionalRepository.findAll()).thenReturn(professionals);

        Booking booking = new Booking();
        booking.setStartTime(LocalDateTime.of(2024, 7, 22, 8, 0));
        booking.setEndTime(LocalDateTime.of(2024, 7, 22, 12, 0)); // Overlaps with the requested duration

        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);

        when(bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(bookings);

        final LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 0);
        final int duration = 4;

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration, 2);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailability_ProfessionalAvailableWithMultipleBookings() {
        when(professionalRepository.findAll()).thenReturn(professionals);

        Booking booking1 = new Booking();
        booking1.setStartTime(LocalDateTime.of(2024, 7, 22, 8, 0));
        booking1.setEndTime(LocalDateTime.of(2024, 7, 22, 9, 0));

        Booking booking2 = new Booking();
        booking2.setStartTime(LocalDateTime.of(2024, 7, 22, 10, 0));
        booking2.setEndTime(LocalDateTime.of(2024, 7, 22, 11, 0));

        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking1);
        bookings.add(booking2);

        when(bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(bookings);

        final LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 11, 31); // 30 minutes after the second booking
        final int duration = 2;

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration, 3);

        assertEquals(25, availableProfessionals.size());
    }

    @Test
    void testCheckAvailability_NoProfessionalsAvailable() {
        when(professionalRepository.findAll()).thenReturn(new ArrayList<>()); // No professionals available

        final LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 0);
        final int duration = 2;

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration, 1);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailability_ProfessionalAvailableInDifferentTimeZones() {
        when(professionalRepository.findAll()).thenReturn(professionals);
        when(bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        final LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 14, 0); // Assume different time zones are considered
        final int duration = 2;

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration, 1);

        assertEquals(25, availableProfessionals.size());
    }

    @Test
    void testCheckAvailability_ProfessionalWithOverlappingBookings() {
        when(professionalRepository.findAll()).thenReturn(professionals);

        Booking booking1 = new Booking();
        booking1.setStartTime(LocalDateTime.of(2024, 7, 22, 8, 0));
        booking1.setEndTime(LocalDateTime.of(2024, 7, 22, 10, 30));

        Booking booking2 = new Booking();
        booking2.setStartTime(LocalDateTime.of(2024, 7, 22, 11, 0));
        booking2.setEndTime(LocalDateTime.of(2024, 7, 22, 13, 0));

        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking1);
        bookings.add(booking2);

        when(bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(bookings);

        final LocalDateTime startTime = LocalDateTime.of(2024, 7, 22, 10, 31); // Overlapping booking scenario
        final int duration = 2;

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, duration, 1);

        assertEquals(0, availableProfessionals.size());
    }
}
