package com.justlife.service;

import com.justlife.model.Booking;
import com.justlife.model.CleaningProfessional;
import com.justlife.repository.BookingRepository;
import com.justlife.repository.CleaningProfessionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    /**
     * Checks the availability of cleaning professionals on a given date.
     *
     * @param date the date to check availability for
     * @param professionalsRequired the number of professionals required
     * @return a list of available cleaning professionals
     */
    public List<CleaningProfessional> checkAvailabilityByDate(LocalDate date, int professionalsRequired) {
        LocalDateTime startOfDay = date.atTime(8, 0);  // Start of the working day at 8:00 AM
        LocalDateTime endOfDay = date.atTime(22, 0);   // End of the working day at 10:00 PM

        List<CleaningProfessional> allProfessionals = professionalRepository.findAll();

        // Filter professionals based on availability
        return allProfessionals.stream()
                .filter(pro -> isAvailableOnDate(pro, startOfDay, endOfDay))
                .limit(professionalsRequired)
                .collect(Collectors.toList());
    }

    /**
     * Checks the availability of cleaning professionals for a given date and time range.
     *
     * @param startTime the start time of the required availability
     * @param duration the duration of the required availability
     * @param professionalsRequired the number of professionals required
     * @return a list of available cleaning professionals
     */
    public List<CleaningProfessional> checkAvailabilityByDateTime(LocalDateTime startTime, int duration, int professionalsRequired) {
        LocalDateTime endTime = startTime.plusHours(duration);
        List<CleaningProfessional> allProfessionals = professionalRepository.findAll();

        // Filter professionals based on availability
        return allProfessionals.stream()
                .filter(pro -> isAvailable(pro, startTime, endTime))
                .limit(professionalsRequired)
                .collect(Collectors.toList());
    }

    /**
     * Checks if a cleaning professional is available on a given date.
     *
     * @param professional the cleaning professional to check availability for
     * @param startOfDay the start of the day to check availability from
     * @param endOfDay the end of the day to check availability until
     * @return true if the professional is available on the given date, false otherwise
     */
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

    /**
     * Checks if a cleaning professional is available for a given time range.
     *
     * @param professional the cleaning professional to check availability for
     * @param startTime the start time of the required availability
     * @param endTime the end time of the required availability
     * @return true if the professional is available for the given time range, false otherwise
     */
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

    /**
     * Updates the availability of professionals after a booking is created.
     *
     * @param professionals the list of professionals to update
     * @param booking the booking information
     */
    public void updateProfessionalsAvailability(List<CleaningProfessional> professionals, Booking booking) {
        for (CleaningProfessional professional : professionals) {
            List<Booking> bookings = bookingRepository.findByProfessionals_Id(professional.getId());
            bookings.add(booking);
            professional.setBookings(bookings);
            professionalRepository.save(professional);
        }
    }
}
