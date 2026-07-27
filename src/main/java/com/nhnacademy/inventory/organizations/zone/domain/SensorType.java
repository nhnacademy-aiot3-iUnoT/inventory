package com.nhnacademy.inventory.organizations.zone.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sensor_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SensorType {
    @Id
    @Column(name = "sensor_type_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sensorTypeId;

    @Column(name = "name", length = 30, nullable = false, unique = true)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Builder
    private SensorType(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
