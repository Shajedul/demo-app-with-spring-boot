//package com.example.battery_api.controller;
//
//import com.example.battery_api.dto.BatteryRequestDTO;
//import com.example.battery_api.dto.BatteryResponseDTO;
//import com.example.battery_api.model.Battery;
//import com.example.battery_api.service.BatteryMapper;
//import com.example.battery_api.service.BatteryService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.List;
//import java.util.Map;
//
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(BatteryController.class)
//class BatteryControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private BatteryService batteryService;
//
//    private BatteryMapper batteryMapper;
//
//    @InjectMocks
//    private BatteryController batteryController;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    private static Battery createBattery(String name, String postcode, int wattCapacity) {
//        Battery battery = new Battery();
//        battery.setName(name);
//        battery.setPostcode(postcode);
//        battery.setWattCapacity(wattCapacity);
//        return battery;
//    }
//
//
//    @Test
//    void saveBatteries_validInput_returnsSavedBatteries() throws Exception {
//        // Arrange
//        BatteryRequestDTO requestDTO1 = new BatteryRequestDTO("Battery1", "1234", 500);
//        BatteryRequestDTO requestDTO2 = new BatteryRequestDTO("Battery2", "5678", 700);
//
//        Battery battery1 = batteryMapper.toEntity(requestDTO1);
//        Battery battery2 = batteryMapper.toEntity(requestDTO1);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/batteries")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(List.of(requestDTO1, requestDTO2))))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.savedBatteriesCount").value(2))
//                .andExpect(jsonPath("$.savedBatteries[0].name").value("Battery1"))
//                .andExpect(jsonPath("$.savedBatteries[1].name").value("Battery2"))
//                .andExpect(jsonPath("$.invalidEntries").isEmpty());
//
//        verify(batteryService, times(1)).publishValidBatteries(List.of(battery1, battery2));
//    }
//
//    @Test
//    void saveBatteries_invalidInput_returnsValidationErrors() throws Exception {
//        // Arrange
//        BatteryRequestDTO invalidRequestDTO = new BatteryRequestDTO("", "5678", -100);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/batteries")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(List.of(invalidRequestDTO))))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.savedBatteriesCount").value(0))
//                .andExpect(jsonPath("$.invalidEntries").isNotEmpty())
//                .andExpect(jsonPath("$.invalidEntries[0].errors.name").isNotEmpty())
//                .andExpect(jsonPath("$.invalidEntries[0].errors.wattCapacity").isNotEmpty());
//    }
//}
