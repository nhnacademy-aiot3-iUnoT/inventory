package com.nhnacademy.inventory.organizations.member.domain;

import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Column(name = "account_uuid", columnDefinition = "BINARY(16)", nullable = false)
    private byte[] accountUuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "organization_role", nullable = false)
    private OrganizationRole organizationRole;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Builder
    private OrganizationMember(Organization organization, byte[] accountUuid,
                               OrganizationRole organizationRole) {
        this.organization = organization;
        this.accountUuid = accountUuid;
        this.organizationRole = organizationRole;
    }

    @PrePersist
    protected void onCreate() {
        this.joinedAt = LocalDateTime.now();
    }
}