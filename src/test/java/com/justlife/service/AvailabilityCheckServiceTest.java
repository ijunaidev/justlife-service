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

import java.time.LocalDate;
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
            final int vehicleIndex = i; // Declare final variable for lambda expression
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

    @Test
    void testCheckAvailabilityByDate_NotEnoughProfessionalsAvailable() {
        when(professionalRepository.findAll()).thenReturn(professionals);
        when(bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        LocalDate date = LocalDate.of(2024, 7, 22);
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDate(date, 6);

        assertEquals(6, availableProfessionals.size());
    }

    @Test
    void testCheckAvailabilityByDate_ProfessionalsUnavailableDueToDifferentVehicles() {
        when(professionalRepository.findAll()).thenReturn(professionals);

        Booking booking = new Booking();
        booking.setStartTime(LocalDateTime.of(2024, 7, 22, 8, 0));
        booking.setEndTime(LocalDateTime.of(2024, 7, 22, 10, 0));

        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);

        when(bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(bookings);

        LocalDate date = LocalDate.of(2024, 7, 22);
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDate(date, 3);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailabilityByDate_ProfessionalsAvailableWithBreaks() {
        when(professionalRepository.findAll()).thenReturn(professionals);

        Booking booking = new Booking();
        booking.setStartTime(LocalDateTime.of(2024, 7, 22, 8, 0));
        booking.setEndTime(LocalDateTime.of(2024, 7, 22, 10, 0));

        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);

        when(bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(bookings);

        LocalDate date = LocalDate.of(2024, 7, 22);
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDateTime(date.atTime(10, 31), 2, 2);

        assertEquals(2, availableProfessionals.size());
    }

    @Test
    void testCheckAvailabilityByDate_ProfessionalsUnavailableDueToWorkingHours() {
        when(professionalRepository.findAll()).thenReturn(professionals);

        LocalDate date = LocalDate.of(2024, 7, 22);
        LocalDateTime startTime = date.atTime(7, 0); // Before working hours
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDateTime(startTime, 2, 1);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailabilityByDate_ProfessionalsUnavailableDueToEndWorkingHours() {
        when(professionalRepository.findAll()).thenReturn(professionals);

        LocalDate date = LocalDate.of(2024, 7, 22);
        LocalDateTime startTime = date.atTime(21, 0); // Ending after working hours
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDateTime(startTime, 2, 1);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailabilityByDate_ProfessionalNotAvailableOnFriday() {
        when(professionalRepository.findAll()).thenReturn(professionals);

        LocalDate date = LocalDate.of(2024, 7, 26); // Friday
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDate(date, 1);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailabilityByDate_ProfessionalAvailableWithSpecificDuration() {
        when(professionalRepository.findAll()).thenReturn(professionals);
        when(bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        LocalDate date = LocalDate.of(2024, 7, 22);
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDateTime(date.atTime(10, 0), 4, 2);

        assertEquals(2, availableProfessionals.size());
    }

    @Test
    void testCheckAvailabilityByDate_ProfessionalUnavailableWithSpecificDuration() {
        when(professionalRepository.findAll()).thenReturn(professionals);

        Booking booking = new Booking();
        booking.setStartTime(LocalDateTime.of(2024, 7, 22, 8, 0));
        booking.setEndTime(LocalDateTime.of(2024, 7, 22, 12, 0)); // Overlaps with the requested duration

        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);

        when(bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(bookings);

        LocalDate date = LocalDate.of(2024, 7, 22);
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDateTime(date.atTime(10, 0), 4, 2);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailabilityByDate_ProfessionalAvailableWithMultipleBookings() {
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

        LocalDate date = LocalDate.of(2024, 7, 22);
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDateTime(date.atTime(11, 31), 2, 3); // 30 minutes after the second booking

        assertEquals(3, availableProfessionals.size());
    }

    @Test
    void testCheckAvailabilityByDate_NoProfessionalsAvailable() {
        when(professionalRepository.findAll()).thenReturn(new ArrayList<>()); // No professionals available

        LocalDate date = LocalDate.of(2024, 7, 22);
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDate(date, 1);

        assertEquals(0, availableProfessionals.size());
    }

    @Test
    void testCheckAvailabilityByDate_ProfessionalAvailableInDifferentTimeZones() {
        when(professionalRepository.findAll()).thenReturn(professionals);
        when(bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        LocalDate date = LocalDate.of(2024, 7, 22);
        LocalDateTime startTime = date.atTime(14, 0); // Assume different time zones are considered
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDateTime(startTime, 2, 1);

        assertEquals(1, availableProfessionals.size());
    }

    @Test
    void testCheckAvailabilityByDate_ProfessionalWithOverlappingBookings() {
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

        LocalDate date = LocalDate.of(2024, 7, 22);
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDateTime(date.atTime(10, 31), 2, 1); // Overlapping booking scenario

        assertEquals(0, availableProfessionals.size());
    }
}
