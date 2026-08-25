package com.nhnacademy.inventory.organizations.department.domain;

import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "departments",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_org_dep",
                    columnNames = {"organization_id", "name"}
            )})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "name", length = 30, nullable = false)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private DepartmentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Department(Organization organization, String name, String description, DepartmentStatus status) {
        this.organization = organization;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public static Department create(Organization organization, String name, String description) {
        return Department.builder()
                .organization(organization)
                .name(name)
                .description(description)
                .status(DepartmentStatus.ACTIVE)
                .build();
    }

    public void updateStatus(DepartmentStatus status) {
        this.status = status;
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
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
