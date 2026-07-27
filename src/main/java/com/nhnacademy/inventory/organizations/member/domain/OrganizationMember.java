package com.nhnacademy.inventory.organizations.member.domain;

import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

    @Column(name = "account_uuid", columnDefinition = "BINARY(16)", nullable = false)
    private UUID accountUuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "organization_role", nullable = false)
    private OrganizationRole organizationRole;

    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Builder
    private OrganizationMember(Organization organization, UUID accountUuid,
                               OrganizationRole organizationRole, Boolean isApproved) {
        this.organization = organization;
        this.accountUuid = accountUuid;
        this.organizationRole = organizationRole;
        this.isApproved = isApproved;
    }

    @PrePersist
    protected void onCreate() {
        this.joinedAt = LocalDateTime.now();
    }
}