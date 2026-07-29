package com.nhnacademy.inventory.organizations.zone.domain;

import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "zones")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "zone_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id", nullable = false)
    private Storage storage;

    @Column(name = "name", length = 30, nullable = false)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ZoneStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "env_status", nullable = false)
    private EnvStatus envStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private Zone(Storage storage, String name, String description, ZoneStatus status, EnvStatus envStatus) {
        this.storage = storage;
        this.name = name;
        this.description = description;
        this.status = status;
        this.envStatus = envStatus;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void close() {
        status = ZoneStatus.CLOSED;
    }

    public void updateInfo(String name, String description){
        this.name = name;
        this.description = description;
    }

    public void changeStatus(ZoneStatus status){
        this.status = status;
    }

    public void changeEnvStatus(EnvStatus envStatus){
        this.envStatus = envStatus;
    }
}
