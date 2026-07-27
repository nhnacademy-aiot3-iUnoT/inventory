package com.nhnacademy.inventory.organizations.member.domain;

import com.nhnacademy.inventory.organizations.organization.domain.OrgPermissionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_permissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberPermission {

    @Id
    @Column(name = "member_permission_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberPermissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_member_id", nullable = false)
    private OrganizationMember organizationMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_permission_type_id", nullable = false)
    private OrgPermissionType permissionType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private MemberPermission(OrganizationMember organizationMember, OrgPermissionType permissionType) {
        this.organizationMember = organizationMember;
        this.permissionType = permissionType;
    }

    @PrePersist
    protected void onCreate() {this.createdAt = LocalDateTime.now();}
}
