package com.justlife.controller;

import com.justlife.model.Vehicle;
import com.justlife.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleService vehicleService;

    private Vehicle vehicle;
    private List<Vehicle> vehicles;

    @BeforeEach
    void setUp() {
        vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setName("Toyota Corolla");

        vehicles = new ArrayList<>();
        vehicles.add(vehicle);
    }

    @Test
    void testGetAllVehicles() throws Exception {
        when(vehicleService.getAllVehicles()).thenReturn(vehicles);

        mockMvc.perform(get("/vehicles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[{'id':1,'name':'Toyota Corolla'}]"));
    }

    @Test
    void testGetVehicleById() throws Exception {
        when(vehicleService.getVehicleById(1L)).thenReturn(Optional.of(vehicle));

        mockMvc.perform(get("/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{'id':1,'name':'Toyota Corolla'}"));
    }

    @Test
    void testGetVehicleById_NotFound() throws Exception {
        when(vehicleService.getVehicleById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/vehicles/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateVehicle() throws Exception {
        when(vehicleService.createVehicle(any(Vehicle.class))).thenReturn(vehicle);

        String vehicleJson = "{\"name\":\"Toyota Corolla\"}";

        mockMvc.perform(post("/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehicleJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{'id':1,'name':'Toyota Corolla'}"));
    }

    @Test
    void testUpdateVehicle() throws Exception {
        when(vehicleService.updateVehicle(eq(1L), any(Vehicle.class)))
                .thenReturn(Optional.of(vehicle));

        String vehicleJson = "{\"name\":\"Toyota Corolla\"}";

        mockMvc.perform(put("/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehicleJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{'id':1,'name':'Toyota Corolla'}"));
    }

    @Test
    void testUpdateVehicle_NotFound() throws Exception {
        when(vehicleService.updateVehicle(eq(1L), any(Vehicle.class)))
                .thenReturn(Optional.empty());

        String vehicleJson = "{\"name\":\"Toyota Corolla\"}";

        mockMvc.perform(put("/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehicleJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteVehicle() throws Exception {
        when(vehicleService.deleteVehicle(1L)).thenReturn(true);

        mockMvc.perform(delete("/vehicles/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteVehicle_NotFound() throws Exception {
        when(vehicleService.deleteVehicle(1L)).thenReturn(false);

        mockMvc.perform(delete("/vehicles/1"))
                .andExpect(status().isNotFound());
    }
}
