package com.justlife.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the booking", example = "1", required = true)
    private Long id;

    @Schema(description = "Start time of the booking", example = "2024-07-22T10:00:00", required = true)
    private LocalDateTime startTime;

    @Schema(description = "End time of the booking", example = "2024-07-22T12:00:00", required = true)
    private LocalDateTime endTime;

    @Schema(description = "Duration of the booking in hours", example = "2", required = true)
    private int duration;

    @Schema(description = "Number of cleaning professionals required for the booking", example = "1", required = true)
    private int professionalsRequired;

    @ManyToMany
    @JoinTable(
            name = "booking_professionals",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "professional_id")
    )
    @Schema(description = "List of cleaning professionals assigned to the booking")
    //@JsonManagedReference
    private List<CleaningProfessional> professionals;
}
