package com.justlife.controller;

import com.justlife.model.Booking;
import com.justlife.model.CleaningProfessional;
import com.justlife.service.AvailabilityCheckService;
import com.justlife.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;


@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private AvailabilityCheckService availabilityCheckService;

    @GetMapping("/availability")
    public List<CleaningProfessional> checkAvailability(@RequestParam String date, @RequestParam(required = false) String startTime, @RequestParam(required = false) Integer duration, @RequestParam(required = false) Integer professionalsRequired) {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate parsedDate = LocalDate.parse(date, dateFormatter);

            if (startTime == null || duration == null || professionalsRequired == null) {
                // Only date provided
                return availabilityCheckService.checkAvailabilityByDate(parsedDate, professionalsRequired != null ? professionalsRequired : 1);
            } else {
                // Date, startTime, duration, and professionalsRequired provided
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                LocalDateTime parsedStartTime = LocalDateTime.parse(startTime, dateTimeFormatter);
                return availabilityCheckService.checkAvailabilityByDateTime(parsedStartTime, duration, professionalsRequired != null ? professionalsRequired : 1);
            }
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format", e);
        }
    }

    @PostMapping
    public Booking createBooking(@RequestBody Booking booking) {
        if (booking.getProfessionalsRequired() < 1 || booking.getProfessionalsRequired() > 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid number of professionals required");
        }
        if (booking.getDuration() != 2 && booking.getDuration() != 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking duration. Must be 2 or 4 hours.");
        }

        return bookingService.createBooking(booking);
    }

    @PutMapping("/{id}")
    public Booking updateBooking(@PathVariable Long id, @RequestBody Booking booking) {
        if (booking.getProfessionalsRequired() < 1 || booking.getProfessionalsRequired() > 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid number of professionals required");
        }
        if (booking.getDuration() != 2 && booking.getDuration() != 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking duration. Must be 2 or 4 hours.");
        }

        return bookingService.updateBooking(id, booking);
    }
}

