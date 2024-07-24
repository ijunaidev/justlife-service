package com.justlife.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int duration;
    private int professionalsRequired;

    @ManyToMany
    @JoinTable(
            name = "booking_professionals",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "professional_id")
    )
    private List<CleaningProfessional> professionals;

}
