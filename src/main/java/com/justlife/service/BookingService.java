package com.justlife.service;

import com.justlife.model.Booking;
import com.justlife.model.CleaningProfessional;
import com.justlife.model.Vehicle;
import com.justlife.repository.BookingRepository;
import com.justlife.repository.CleaningProfessionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CleaningProfessionalRepository professionalRepository;

    @Autowired
    private AvailabilityCheckService availabilityCheckService;

    @Transactional
    public Booking createBooking(Booking booking) {
        // Validate duration and professionals count
        if ((booking.getDuration() != 2 && booking.getDuration() != 4) ||
                (booking.getProfessionalsRequired() < 1 || booking.getProfessionalsRequired() > 3)) {
            throw new IllegalArgumentException("Invalid booking duration or professionals required");
        }

        // Calculate end time from start time and duration
        LocalDateTime startTime = booking.getStartTime();
        LocalDateTime endTime = startTime.plusHours(booking.getDuration());
        booking.setEndTime(endTime);

        // Check availability of professionals assigned to the same vehicle
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, booking.getDuration(), booking.getProfessionalsRequired());

        if (availableProfessionals.size() < booking.getProfessionalsRequired()) {
            throw new IllegalStateException("Not enough professionals available for the requested time");
        }

        // Filter professionals by vehicle
        Vehicle vehicle = availableProfessionals.get(0).getVehicle();
        List<CleaningProfessional> assignedProfessionals = vehicle.getProfessionals().stream()
                .filter(pro -> availableProfessionals.contains(pro))
                .limit(booking.getProfessionalsRequired())
                .collect(Collectors.toList());

        if (assignedProfessionals.size() < booking.getProfessionalsRequired()) {
            throw new IllegalStateException("Not enough professionals from the same vehicle available for the requested time");
        }

        booking.setProfessionals(assignedProfessionals);

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking updateBooking(Long bookingId, Booking updatedBooking) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);

        if (!optionalBooking.isPresent()) {
            throw new IllegalStateException("Booking not found");
        }

        Booking existingBooking = optionalBooking.get();

        // Validate duration and professionals count
        if ((updatedBooking.getDuration() != 2 && updatedBooking.getDuration() != 4) ||
                (updatedBooking.getProfessionalsRequired() < 1 || updatedBooking.getProfessionalsRequired() > 3)) {
            throw new IllegalArgumentException("Invalid booking duration or professionals required");
        }

        // Calculate end time from start time and duration
        LocalDateTime startTime = updatedBooking.getStartTime();
        LocalDateTime endTime = startTime.plusHours(updatedBooking.getDuration());
        updatedBooking.setEndTime(endTime);

        // Check availability of professionals assigned to the same vehicle
        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, updatedBooking.getDuration(), updatedBooking.getProfessionalsRequired());

        if (availableProfessionals.size() < updatedBooking.getProfessionalsRequired()) {
            throw new IllegalStateException("Not enough professionals available for the requested time");
        }

        // Filter professionals by vehicle
        Vehicle vehicle = availableProfessionals.get(0).getVehicle();
        List<CleaningProfessional> assignedProfessionals = vehicle.getProfessionals().stream()
                .filter(pro -> availableProfessionals.contains(pro))
                .limit(updatedBooking.getProfessionalsRequired())
                .collect(Collectors.toList());

        if (assignedProfessionals.size() < updatedBooking.getProfessionalsRequired()) {
            throw new IllegalStateException("Not enough professionals from the same vehicle available for the requested time");
        }

        existingBooking.setProfessionals(assignedProfessionals);
        existingBooking.setStartTime(updatedBooking.getStartTime());
        existingBooking.setDuration(updatedBooking.getDuration());
        existingBooking.setProfessionalsRequired(updatedBooking.getProfessionalsRequired());

        return bookingRepository.save(existingBooking);
    }
}
