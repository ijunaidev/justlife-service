package com.justlife.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the vehicle", example = "1", required = true)
    private Long id;

    @Schema(description = "Name of the vehicle", example = "Toyota Corolla", required = true)
    private String name;

    @OneToMany(mappedBy = "vehicle")
    @Schema(description = "List of cleaning professionals assigned to the vehicle")
    //@JsonBackReference
    private List<CleaningProfessional> professionals;
}
