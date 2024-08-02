package com.justlife.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class BookingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the booking detail", example = "1", required = true)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    @Schema(description = "The booking associated with the booking detail", required = true)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "professional_id")
    @Schema(description = "The cleaning professional associated with the booking detail", required = true)
    private CleaningProfessional cleaningProfessional;
}
