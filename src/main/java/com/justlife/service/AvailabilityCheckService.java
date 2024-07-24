package com.justlife.service;

import com.justlife.model.Booking;
import com.justlife.model.CleaningProfessional;
import com.justlife.repository.BookingRepository;
import com.justlife.repository.CleaningProfessionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvailabilityCheckService {

    @Autowired
    private CleaningProfessionalRepository professionalRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public List<CleaningProfessional> checkAvailability(LocalDateTime startTime, int duration, int professionalsRequired) {
        LocalDateTime endTime = startTime.plusHours(duration);
        List<CleaningProfessional> allProfessionals = professionalRepository.findAll();

        List<CleaningProfessional> availableProfessionals = allProfessionals.stream()
                .filter(professional -> isAvailable(professional, startTime, endTime, professionalsRequired))
                .collect(Collectors.toList());

        if (availableProfessionals.size() < professionalsRequired) {
            return new ArrayList<>();
        }

        return availableProfessionals;
    }

    private boolean isAvailable(CleaningProfessional professional, LocalDateTime startTime, LocalDateTime endTime, int professionalsRequired) {
        boolean isWorkingDay = professional.isWorkingOnFridays() || startTime.getDayOfWeek().getValue() != 5;

        if (!isWorkingDay) {
            return false;
        }

        String[] workingHours = professional.getWorkingHours().split("-");
        LocalTime startWorkTime = LocalTime.parse(workingHours[0]);
        LocalTime endWorkTime = LocalTime.parse(workingHours[1]);

        boolean isWithinWorkingHours = !startTime.toLocalTime().isBefore(startWorkTime) && !endTime.toLocalTime().isAfter(endWorkTime);

        if (!isWithinWorkingHours) {
            return false;
        }

        List<Booking> bookings = bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(
                professional.getId(), startTime.toLocalDate().atStartOfDay(), endTime.toLocalDate().atTime(23, 59));

        for (Booking booking : bookings) {
            LocalDateTime bookingStart = booking.getStartTime();
            LocalDateTime bookingEnd = booking.getEndTime();

            if ((startTime.isBefore(bookingEnd.plusMinutes(30)) && endTime.isAfter(bookingStart.minusMinutes(30)))) {
                return false;
            }
        }

        return true;
    }
}
