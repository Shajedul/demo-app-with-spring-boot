package com.example.battery_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Setter;

@Data
public class BatteryResponseDTO {

    @JsonProperty("id")
    private Long id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("postcode")
    private String postcode;
    @JsonProperty("wattCapacity")
    private int wattCapacity;

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public void setWattCapacity(int wattCapacity) {
        this.wattCapacity = wattCapacity;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
