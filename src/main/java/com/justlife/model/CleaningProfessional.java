package com.justlife.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CleaningProfessional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private boolean available;
    private String workingHours;
    private boolean workingOnFridays;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;
}