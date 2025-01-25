package com.example.battery_api.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Battery entity representing the battery details.
 */
@Entity
@Data
public class Battery {

    /**
     * Unique identifier for the battery.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the battery.
     * This field cannot be null.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Postcode where the battery is located.
     * This field cannot be null.
     */
    @Column(nullable = false)
    private String postcode;

    /**
     * Watt capacity of the battery.
     * This field cannot be null.
     */
    @Column(nullable = false)
    private int wattCapacity;

    // Getters and setters for the fields

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public int getWattCapacity() {
        return wattCapacity;
    }

    public void setWattCapacity(int wattCapacity) {
        this.wattCapacity = wattCapacity;
    }

    @Override
    public String toString() {
        return "Battery{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", postcode='" + postcode + '\'' +
                ", wattCapacity=" + wattCapacity +
                '}';
    }
}
