package com.justlife.service;

import com.justlife.model.Booking;
import com.justlife.model.BookingDetail;
import com.justlife.model.CleaningProfessional;
import com.justlife.model.Vehicle;
import com.justlife.repository.BookingDetailRepository;
import com.justlife.repository.BookingRepository;
import com.justlife.repository.CleaningProfessionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingDetailRepository bookingDetailRepository;

    @Autowired
    private AvailabilityCheckService availabilityCheckService;

    @Transactional
    public Booking createBooking(Booking booking) {
        validateBooking(booking);

        LocalDateTime startTime = booking.getStartTime();
        LocalDateTime endTime = startTime.plusHours(booking.getDuration());
        booking.setEndTime(endTime);

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDateTime(startTime, booking.getDuration(), booking.getProfessionalsRequired());

        if (availableProfessionals.size() < booking.getProfessionalsRequired()) {
            throw new IllegalStateException("Not enough professionals available for the requested time");
        }

        Vehicle vehicle = availableProfessionals.get(0).getVehicle();
        List<CleaningProfessional> assignedProfessionals = filterProfessionalsByVehicle(vehicle.getId(), availableProfessionals, booking.getProfessionalsRequired());

        Booking savedBooking = bookingRepository.save(booking);

        availabilityCheckService.updateProfessionalsAvailability(assignedProfessionals, savedBooking);

        return savedBooking;
    }

    @Transactional
    public Booking updateBooking(Long bookingId, Booking updatedBooking) {
        validateBooking(updatedBooking);

        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found"));

        LocalDateTime startTime = updatedBooking.getStartTime();
        LocalDateTime endTime = startTime.plusHours(updatedBooking.getDuration());
        updatedBooking.setEndTime(endTime);

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailabilityByDateTime(startTime, updatedBooking.getDuration(), updatedBooking.getProfessionalsRequired());

        if (availableProfessionals.size() < updatedBooking.getProfessionalsRequired()) {
            throw new IllegalStateException("Not enough professionals available for the requested time");
        }

        Vehicle vehicle = availableProfessionals.get(0).getVehicle();
        List<CleaningProfessional> assignedProfessionals = filterProfessionalsByVehicle(vehicle.getId(), availableProfessionals, updatedBooking.getProfessionalsRequired());

        existingBooking.setStartTime(updatedBooking.getStartTime());
        existingBooking.setEndTime(updatedBooking.getEndTime());
        existingBooking.setDuration(updatedBooking.getDuration());
        existingBooking.setProfessionalsRequired(updatedBooking.getProfessionalsRequired());

        Booking savedBooking = bookingRepository.save(existingBooking);

        availabilityCheckService.updateProfessionalsAvailability(assignedProfessionals, savedBooking);

        return savedBooking;
    }

    private void validateBooking(Booking booking) {
        if (booking.getDuration() != 2 && booking.getDuration() != 4) {
            throw new IllegalArgumentException("Invalid booking duration. Must be 2 or 4 hours.");
        }
        if (booking.getProfessionalsRequired() < 1 || booking.getProfessionalsRequired() > 3) {
            throw new IllegalArgumentException("Invalid number of professionals required. Must be between 1 and 3.");
        }
    }

    private List<CleaningProfessional> filterProfessionalsByVehicle(Long vehicleId, List<CleaningProfessional> availableProfessionals, int professionalsRequired) {
        List<CleaningProfessional> assignedProfessionals = availableProfessionals.stream()
                .filter(pro -> pro.getVehicle().getId().equals(vehicleId))
                .limit(professionalsRequired)
                .collect(Collectors.toList());

        if (assignedProfessionals.size() < professionalsRequired) {
            throw new IllegalStateException("Not enough professionals from the same vehicle available for the requested time");
        }

        return assignedProfessionals;
    }

    public List<BookingDetail> getAllBookingDetails() {
        return bookingDetailRepository.findAll();
    }
}
