package com.nhnacademy.inventory.organizations.invitation.domain;

import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

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

    @Column(name = "token", columnDefinition = "BINARY(16)", nullable = false, unique = true)
    private UUID token;

    @Enumerated(EnumType.STRING)
    @Column(name = "invitation_status", nullable = false)
    private InvitationStatus invitationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "invitation_type", nullable = false)
    private InvitationType invitationType;

    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "reissued_at")
    private LocalDateTime reissuedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Invitation(Organization organization, String email, UUID token, InvitationType invitationType) {
        this.organization = organization;
        this.email = email;
        this.token = token;
        this.invitationType = invitationType;
        this.invitationStatus = InvitationStatus.ACTIVE;
    }

    public static Invitation create(Organization organization, String email, InvitationType invitationType) {
        return Invitation.builder()
                .organization(organization)
                .email(email)
                .token(UUID.randomUUID())
                .invitationType(invitationType)
                .build();
    }

    public void markEmailSent() {
        this.emailSentAt = LocalDateTime.now();
    }

    public void cancel() {
        this.invitationStatus = InvitationStatus.CANCELED;
    }

    public void use() {
        this.invitationStatus = InvitationStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    public void restore() {
        this.invitationStatus = InvitationStatus.ACTIVE;
        this.usedAt = null;
    }

    public void expire() {
        this.invitationStatus = InvitationStatus.EXPIRED;
    }

    public boolean isExpired(LocalDateTime now) {
        return expiredAt.isBefore(now);
    }

    public void markReissued() {
        this.reissuedAt = LocalDateTime.now();
    }

    public boolean isReissued() {
        return reissuedAt != null;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.expiredAt = createdAt.plusDays(1);
    }
}
