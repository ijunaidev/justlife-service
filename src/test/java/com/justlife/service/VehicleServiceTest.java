package com.justlife.service;

import com.justlife.model.Vehicle;
import com.justlife.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle vehicle;
    private List<Vehicle> vehicles;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setName("Toyota Corolla");

        vehicles = new ArrayList<>();
        vehicles.add(vehicle);
    }

    @Test
    void testGetAllVehicles() {
        when(vehicleRepository.findAll()).thenReturn(vehicles);

        List<Vehicle> result = vehicleService.getAllVehicles();
        assertEquals(1, result.size());
        assertEquals(vehicle, result.get(0));
    }

    @Test
    void testGetVehicleById() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        Optional<Vehicle> result = vehicleService.getVehicleById(1L);
        assertTrue(result.isPresent());
        assertEquals(vehicle, result.get());
    }

    @Test
    void testGetVehicleById_NotFound() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Vehicle> result = vehicleService.getVehicleById(1L);
        assertFalse(result.isPresent());
    }

    @Test
    void testCreateVehicle() {
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        Vehicle result = vehicleService.createVehicle(vehicle);
        assertEquals(vehicle, result);
    }

    @Test
    void testUpdateVehicle() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        Optional<Vehicle> result = vehicleService.updateVehicle(1L, vehicle);
        assertTrue(result.isPresent());
        assertEquals(vehicle, result.get());
    }

    @Test
    void testUpdateVehicle_NotFound() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Vehicle> result = vehicleService.updateVehicle(1L, vehicle);
        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteVehicle() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        doNothing().when(vehicleRepository).deleteById(1L);

        boolean result = vehicleService.deleteVehicle(1L);
        assertTrue(result);
    }

    @Test
    void testDeleteVehicle_NotFound() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = vehicleService.deleteVehicle(1L);
        assertFalse(result);
    }
}
