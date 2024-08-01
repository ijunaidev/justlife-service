package com.justlife.service;

import com.justlife.model.Vehicle;
import com.justlife.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleRepository.findById(id);
    }

    public Vehicle createVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public Optional<Vehicle> updateVehicle(Long id, Vehicle vehicle) {
        return vehicleRepository.findById(id).map(existingVehicle -> {
            vehicle.setId(id);
            return vehicleRepository.save(vehicle);
        });
    }

    public boolean deleteVehicle(Long id) {
        return vehicleRepository.findById(id).map(vehicle -> {
            vehicleRepository.deleteById(id);
            return true;
        }).orElse(false);
    }
}
