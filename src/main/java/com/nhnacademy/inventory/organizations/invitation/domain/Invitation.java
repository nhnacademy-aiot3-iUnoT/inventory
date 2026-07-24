package com.nhnacademy.inventory.organizations.invitation.domain;

import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "invitations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    @Column(name = "token", length = 36, nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "invitation_status", nullable = false)
    private InvitationStatus invitationStatus;

    @Column(name = "is_email_sent", nullable = false)
    private Boolean isEmailSent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Builder
    private Invitation(Organization organization, String email, String token,
                       InvitationStatus invitationStatus, Boolean isEmailSent, LocalDateTime expiredAt) {
        this.organization = organization;
        this.email = email;
        this.token = token;
        this.invitationStatus = invitationStatus;
        this.isEmailSent = isEmailSent;
        this.expiredAt = expiredAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}