package com.nhnacademy.inventory.organizations.organization.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "organizations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organization_id")
    private Long id;

    @Column(name = "business_number", length = 10, nullable = false, unique = true)
    private String businessNumber;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "road_address", length = 100)
    private String roadAddress;

    @Column(name = "zip_code", length = 5)
    private String zipCode;

    @Column(name = "address_detail", length = 50)
    private String addressDetail;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrganizationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private Organization(String businessNumber, String name, String roadAddress, String zipCode,
                         String addressDetail, String description, OrganizationStatus status) {
        this.businessNumber = businessNumber;
        this.name = name;
        this.roadAddress = roadAddress;
        this.zipCode = zipCode;
        this.addressDetail = addressDetail;
        this.description = description;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
