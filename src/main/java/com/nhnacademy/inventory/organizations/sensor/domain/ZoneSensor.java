package com.nhnacademy.inventory.organizations.sensor.domain;

import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "zone_sensors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ZoneSensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "zone_sensor_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(name = "device_eui", length = 50, nullable = false, unique = true)
    private String deviceEui;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Builder
    private ZoneSensor(Zone zone, String deviceEui, String name, String description) {
        this.zone = zone;
        this.deviceEui = deviceEui;
        this.name = name;
        this.description = description;
    }

    public void updateInfo(String name, String description){
        this.name = name;
        this.description = description;
    }
}
