package com.justlife.service;

import com.justlife.model.Booking;
import com.justlife.model.CleaningProfessional;
import com.justlife.model.Vehicle;
import com.justlife.repository.BookingRepository;
import com.justlife.repository.CleaningProfessionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public List<CleaningProfessional> checkAvailabilityByDate(LocalDate date, int professionalsRequired) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59);

        List<CleaningProfessional> allProfessionals = professionalRepository.findAll();

        // Filter professionals based on availability
        return allProfessionals.stream()
                .filter(pro -> isAvailableOnDate(pro, startOfDay, endOfDay))
                .limit(professionalsRequired)
                .collect(Collectors.toList());
    }

    public List<CleaningProfessional> checkAvailabilityByDateTime(LocalDateTime startTime, int duration, int professionalsRequired) {
        LocalDateTime endTime = startTime.plusHours(duration);
        List<CleaningProfessional> allProfessionals = professionalRepository.findAll();

        // Filter professionals based on availability
        return allProfessionals.stream()
                .filter(pro -> isAvailable(pro, startTime, endTime))
                .limit(professionalsRequired)
                .collect(Collectors.toList());
    }

    private boolean isAvailableOnDate(CleaningProfessional professional, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        // Check if the professional is working on the given day
        boolean isWorkingDay = professional.isWorkingOnFridays() || startOfDay.getDayOfWeek().getValue() != 5;

        if (!isWorkingDay) {
            return false;
        }

        // Check working hours
        String[] workingHours = professional.getWorkingHours().split("-");
        LocalTime startWorkTime = LocalTime.parse(workingHours[0]);
        LocalTime endWorkTime = LocalTime.parse(workingHours[1]);

        boolean isWithinWorkingHours = !startOfDay.toLocalTime().isBefore(startWorkTime) && !endOfDay.toLocalTime().isAfter(endWorkTime);

        if (!isWithinWorkingHours) {
            return false;
        }

        // Check for existing bookings and ensure a 30-minute break
        List<Booking> bookings = bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(
                professional.getId(), startOfDay, endOfDay);

        for (Booking booking : bookings) {
            LocalDateTime bookingStart = booking.getStartTime();
            LocalDateTime bookingEnd = booking.getEndTime();

            if (startOfDay.isBefore(bookingEnd.plusMinutes(30)) && endOfDay.isAfter(bookingStart.minusMinutes(30))) {
                return false;
            }
        }

        return true;
    }

    private boolean isAvailable(CleaningProfessional professional, LocalDateTime startTime, LocalDateTime endTime) {
        // Check if the professional is working on the given day
        boolean isWorkingDay = professional.isWorkingOnFridays() || startTime.getDayOfWeek().getValue() != 5;

        if (!isWorkingDay) {
            return false;
        }

        // Check working hours
        String[] workingHours = professional.getWorkingHours().split("-");
        LocalTime startWorkTime = LocalTime.parse(workingHours[0]);
        LocalTime endWorkTime = LocalTime.parse(workingHours[1]);

        boolean isWithinWorkingHours = !startTime.toLocalTime().isBefore(startWorkTime) && !endTime.toLocalTime().isAfter(endWorkTime);

        if (!isWithinWorkingHours) {
            return false;
        }

        // Check for existing bookings and ensure a 30-minute break
        List<Booking> bookings = bookingRepository.findByProfessionals_IdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(
                professional.getId(), startTime.toLocalDate().atStartOfDay(), endTime.toLocalDate().atTime(23, 59));

        for (Booking booking : bookings) {
            LocalDateTime bookingStart = booking.getStartTime();
            LocalDateTime bookingEnd = booking.getEndTime();

            if (startTime.isBefore(bookingEnd.plusMinutes(30)) && endTime.isAfter(bookingStart.minusMinutes(30))) {
                return false;
            }
        }

        return true;
    }

    public void updateProfessionalsAvailability(List<CleaningProfessional> professionals, Booking booking) {
        for (CleaningProfessional professional : professionals) {
            List<Booking> bookings = bookingRepository.findByProfessionals_Id(professional.getId());
            bookings.add(booking);
            professional.setBookings(bookings);
            professionalRepository.save(professional);
        }
    }
}
