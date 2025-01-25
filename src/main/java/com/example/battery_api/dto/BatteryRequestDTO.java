package com.example.battery_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


@Data
public class BatteryRequestDTO {
    @NotBlank(message = "Name cannot be blank")
    @NotNull(message = "Name can not be null")
    private String name;

    @NotBlank(message = "Postcode cannot be null or blank")
    @Pattern(regexp = "^(0[2-9][0-9]{2}|[1-9][0-9]{3})$", message = "Postcode must be between 0200 and 9999")
    private String postcode;

    @NotNull(message = "Watt capacity is required")
    @Min(value = 1, message = "Watt capacity must be at least 1 KW")
    @Max(value = 1000, message = "Maximum watt capacity can be at least 1000 KW")
    private Integer wattCapacity;

    public String getName() {
        return name;
    }

    public String getPostcode() {
        return postcode;
    }

    public Integer getWattCapacity() {
        return wattCapacity;
    }


    @Override
    public String toString() {
        return "BatteryRequestDTO{" +
                "name='" + name + '\'' +
                ", postcode='" + postcode + '\'' +
                ", wattCapacity=" + wattCapacity +
                '}';
    }
}
