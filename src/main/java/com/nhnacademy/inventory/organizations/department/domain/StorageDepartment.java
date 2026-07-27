package com.nhnacademy.inventory.organizations.department.domain;

import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "storage_departments",
            uniqueConstraints = {
                @UniqueConstraint(name = "uk_storage_dep",
                                    columnNames = {"storage_id", "department_id"}
                )
            })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StorageDepartment {

    @Id
    @Column(name = "storage_department_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storageDepartmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id")
    private Storage storage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    private StorageDepartment(Storage storage, Department department) {
        this.storage = storage;
        this.department = department;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
