package com.justlife.service;

import com.justlife.model.Booking;
import com.justlife.model.CleaningProfessional;
import com.justlife.repository.BookingRepository;
import com.justlife.repository.CleaningProfessionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvailabilityCheckService {

    @Autowired
    private CleaningProfessionalRepository professionalRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public List<CleaningProfessional> checkAvailability(LocalDateTime startTime, int duration) {
        LocalDateTime endTime = startTime.plusHours(duration);
        List<CleaningProfessional> allProfessionals = professionalRepository.findAll();

        return allProfessionals.stream()
                .filter(professional -> isAvailable(professional, startTime, endTime))
                .collect(Collectors.toList());
    }

    private boolean isAvailable(CleaningProfessional professional, LocalDateTime startTime, LocalDateTime endTime) {
        String[] workingHours = professional.getWorkingHours().split("-");
        LocalTime startWorkTime = LocalTime.parse(workingHours[0]);
        LocalTime endWorkTime = LocalTime.parse(workingHours[1]);

        boolean isWorkingDay = professional.isWorkingOnFridays() || startTime.getDayOfWeek().getValue() != 5;

        boolean isWithinWorkingHours = !startTime.toLocalTime().isBefore(startWorkTime) && !endTime.toLocalTime().isAfter(endWorkTime);

        boolean isNotBooked = checkIfNotBooked(professional, startTime, endTime);

        return isWorkingDay && isWithinWorkingHours && isNotBooked;
    }

    private boolean checkIfNotBooked(CleaningProfessional professional, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime adjustedStartTime = startTime.minusMinutes(30);
        LocalDateTime adjustedEndTime = endTime.plusMinutes(30);

        List<Booking> conflictingBookings = bookingRepository.findByStartTimeLessThanEqualAndEndTimeGreaterThanEqual(adjustedEndTime, adjustedStartTime);

        return conflictingBookings.stream()
                .noneMatch(booking -> booking.getProfessionals() != null && booking.getProfessionals().contains(professional));
    }
}
