package com.nhnacademy.inventory.organizations.notification.domain;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_channel_preferences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationChannelPreference {

    public static NotificationChannelPreference create(
            OrganizationMember organizationMember,
            NotificationChannel channel,
            String recipient,
            boolean enabled
    ) {
        NotificationChannelPreference preference = new NotificationChannelPreference();
        preference.organizationMember = organizationMember;
        preference.channel = channel;
        preference.recipient = recipient;
        preference.isEnabled = enabled;
        return preference;
    }

    public void update(String recipient, boolean enabled) {
        this.recipient = recipient;
        this.isEnabled = enabled;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_channel_preference_id")
    private Long channelPreferenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_member_id", nullable = false)
    private OrganizationMember organizationMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 30)
    private NotificationChannel channel;

    @Column(name = "recipient", nullable = true, length = 255)
    private String recipient;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
