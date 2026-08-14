package com.nhnacademy.inventory.organizations.member.domain;

import com.nhnacademy.inventory.organizations.department.domain.MemberDepartment;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "organization_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrganizationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organization_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "account_uuid", columnDefinition = "BINARY(16)", nullable = false, unique = true)
    private UUID accountUuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "organization_role", nullable = false)
    private OrganizationRole organizationRole;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private OrganizationMember(Organization organization, UUID accountUuid, OrganizationRole organizationRole) {
        this.organization = organization;
        this.accountUuid = accountUuid;
        this.organizationRole = organizationRole;
    }

    public static OrganizationMember createUser(Organization organization, UUID accountUuid, OrganizationRole role) {
        return OrganizationMember.builder()
                .organization(organization)
                .accountUuid(accountUuid)
                .organizationRole(role)
                .build();
    }


    public boolean isOwner() {
        return this.organizationRole == OrganizationRole.ORG_OWNER;
    }

    public boolean isBoss() {
        return this.organizationRole == OrganizationRole.ORG_BOSS;
    }

    @PrePersist
    protected void onCreate() {
        this.joinedAt = LocalDateTime.now();
    }

    public void changeRole(OrganizationRole role) {
        this.organizationRole = role;
    }
}
