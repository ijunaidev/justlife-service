package com.justlife.service;

import com.justlife.model.Booking;
import com.justlife.model.CleaningProfessional;
import com.justlife.repository.BookingRepository;
import com.justlife.repository.CleaningProfessionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AvailabilityCheckService availabilityCheckService;

    @Transactional
    public Booking createBooking(Booking booking) {
        LocalDateTime startTime = booking.getStartTime();
        booking.setEndTime(startTime.plusHours(booking.getDuration()));

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, booking.getDuration());

        if (availableProfessionals.size() < booking.getProfessionalsRequired()) {
            throw new IllegalStateException("Not enough professionals available for the requested time");
        }

        List<CleaningProfessional> assignedProfessionals = availableProfessionals.subList(0, booking.getProfessionalsRequired());
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

        LocalDateTime startTime = updatedBooking.getStartTime();
        LocalDateTime endTime = startTime.plusHours(updatedBooking.getDuration());

        List<CleaningProfessional> availableProfessionals = availabilityCheckService.checkAvailability(startTime, updatedBooking.getDuration());

        if (availableProfessionals.size() < updatedBooking.getProfessionalsRequired()) {
            throw new IllegalStateException("Not enough professionals available for the requested time");
        }

        List<CleaningProfessional> assignedProfessionals = availableProfessionals.subList(0, updatedBooking.getProfessionalsRequired());
        existingBooking.setProfessionals(assignedProfessionals);
        existingBooking.setStartTime(updatedBooking.getStartTime());
        existingBooking.setDuration(updatedBooking.getDuration());
        existingBooking.setProfessionalsRequired(updatedBooking.getProfessionalsRequired());

        return bookingRepository.save(existingBooking);
    }
}
