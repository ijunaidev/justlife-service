package com.justlife.controller;

import com.justlife.model.Booking;
import com.justlife.model.BookingDetail;
import com.justlife.model.CleaningProfessional;
import com.justlife.service.AvailabilityCheckService;
import com.justlife.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Check availability of cleaning professionals", description = "Returns a list of available cleaning professionals based on the given date and time parameters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of available professionals"),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters")
    })
    @GetMapping("/availability")
    public List<CleaningProfessional> checkAvailability(@RequestParam String date, @RequestParam(required = false) String startTime, @RequestParam(required = false) Integer duration, @RequestParam(required = false) Integer professionalsRequired) {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate parsedDate = LocalDate.parse(date, dateFormatter);

            professionalsRequired = professionalsRequired != null ? professionalsRequired : 1;

            if (professionalsRequired < 1 || professionalsRequired > 3) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid number of professionals required");
            }

            if (duration != null && duration != 2 && duration != 4) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking duration. Must be 2 or 4 hours.");
            }

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

    @Operation(summary = "Create a new booking", description = "Creates a new booking for cleaning services.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created booking"),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters")
    })
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

    @Operation(summary = "Update an existing booking", description = "Updates an existing booking for cleaning services.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated booking"),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
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

    @Operation(summary = "Get all bookings", description = "Returns a list of all bookings.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of all bookings")
    })
    @GetMapping
    public List<BookingDetail> getAllBookingDetails() {
        return bookingService.getAllBookingDetails();
    }
}
