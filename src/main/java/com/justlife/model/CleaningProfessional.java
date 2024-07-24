package com.justlife.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class CleaningProfessional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the cleaning professional", example = "1", required = true)
    private Long id;

    @Schema(description = "Name of the cleaning professional", example = "Alice", required = true)
    private String name;

    @Schema(description = "Phone number of the cleaning professional", example = "123-456-7890", required = true)
    private boolean available;

    @Schema(description = "Working hours of the cleaning professional", example = "08:00-22:00", required = true)
    private String workingHours = "08:00-22:00";

    @Schema(description = "Whether the cleaning professional works on Fridays", example = "false", required = true)
    private boolean workingOnFridays = false;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    @Schema(description = "Vehicle assigned to the cleaning professional", required = true)
    private Vehicle vehicle;

    @ManyToMany(mappedBy = "professionals")
    @Schema(description = "List of bookings assigned to the cleaning professional")
    private List<Booking> bookings;

}
