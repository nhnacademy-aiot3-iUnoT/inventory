package com.nhnacademy.inventory.organizations.organization.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "org_permission_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrgPermissionType {

    @Id
    @Column(name = "org_permission_type_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orgPermissionTypeId;

    @Column(name = "name", length = 30, nullable = false, unique = true)
    private String name;

    @Builder
    private OrgPermissionType(String name) {
        this.name = name;
    }
}
