package com.example.battery_api.service;

import com.example.battery_api.dto.BatteryRequestDTO;
import com.example.battery_api.dto.BatteryResponseDTO;
import com.example.battery_api.model.Battery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BatteryMapper {

    public Battery toEntity(BatteryRequestDTO dto) {
        Battery battery = new Battery();
        battery.setName(dto.getName());
        battery.setPostcode(dto.getPostcode());
        battery.setWattCapacity(dto.getWattCapacity());

        return battery;
    }

    public BatteryResponseDTO toDTO(Battery battery) {
        BatteryResponseDTO dto = new BatteryResponseDTO();
        dto.setName(battery.getName());
        dto.setPostcode(battery.getPostcode());
        dto.setWattCapacity(battery.getWattCapacity());
        dto.setId(battery.getId());
        return dto;
    }

    public List<BatteryResponseDTO> toDTOList(List<Battery> batteries) {
        return batteries.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
