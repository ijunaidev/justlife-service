package com.justlife.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class CleaningProfessional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private boolean available;
    private String workingHours = "08:00-22:00";
    private boolean workingOnFridays = false;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToMany(mappedBy = "professionals")
    private List<Booking> bookings;

}
