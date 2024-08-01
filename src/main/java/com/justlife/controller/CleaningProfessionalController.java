package com.justlife.controller;

import com.justlife.model.CleaningProfessional;
import com.justlife.service.CleaningProfessionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cleaningProfessionals")
public class CleaningProfessionalController {

    @Autowired
    private CleaningProfessionalService cleaningProfessionalService;

    @Operation(summary = "Get all cleaning professionals", description = "Retrieve a list of all cleaning professionals.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    })
    @GetMapping
    public List<CleaningProfessional> getAllCleaningProfessionals() {
        return cleaningProfessionalService.getAllCleaningProfessionals();
    }

    @Operation(summary = "Get a cleaning professional by ID", description = "Retrieve a cleaning professional by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved cleaning professional"),
            @ApiResponse(responseCode = "404", description = "Cleaning professional not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CleaningProfessional> getCleaningProfessionalById(@PathVariable Long id) {
        Optional<CleaningProfessional> cleaningProfessional = cleaningProfessionalService.getCleaningProfessionalById(id);
        return cleaningProfessional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "Create a new cleaning professional", description = "Create a new cleaning professional.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created cleaning professional")
    })
    @PostMapping
    public ResponseEntity<CleaningProfessional> createCleaningProfessional(@RequestBody CleaningProfessional cleaningProfessional) {
        CleaningProfessional savedCleaningProfessional = cleaningProfessionalService.createCleaningProfessional(cleaningProfessional);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCleaningProfessional);
    }

    @Operation(summary = "Update a cleaning professional", description = "Update an existing cleaning professional.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated cleaning professional"),
            @ApiResponse(responseCode = "404", description = "Cleaning professional not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CleaningProfessional> updateCleaningProfessional(@PathVariable Long id, @RequestBody CleaningProfessional cleaningProfessional) {
        Optional<CleaningProfessional> updatedCleaningProfessional = cleaningProfessionalService.updateCleaningProfessional(id, cleaningProfessional);
        return updatedCleaningProfessional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "Delete a cleaning professional", description = "Delete a cleaning professional by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted cleaning professional"),
            @ApiResponse(responseCode = "404", description = "Cleaning professional not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCleaningProfessional(@PathVariable Long id) {
        boolean isDeleted = cleaningProfessionalService.deleteCleaningProfessional(id);
        return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
