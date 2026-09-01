package com.nhnacademy.inventory.assistant.domain;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "assistant_notes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssistantNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assistant_note_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_member_id", nullable = false)
    private OrganizationMember organizationMember;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "severity", length = 20, nullable = false)
    private Severity severity;

    @Column(name = "message", length = 1000, nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "target_type", length = 20)
    private TargetType targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private AssistantNote(OrganizationMember organizationMember, Severity severity, String message,
                          TargetType targetType, Long targetId) {
        this.organizationMember = organizationMember;
        this.severity = severity;
        this.message = message;
        this.targetType = targetType;
        this.targetId = targetId;
        this.read = false;
        this.createdAt = LocalDateTime.now();
    }

    public static AssistantNote of(OrganizationMember organizationMember, Severity severity, String message,
                                   TargetType targetType, Long targetId) {
        return new AssistantNote(organizationMember, severity, message, targetType, targetId);
    }

    public void markRead() {
        this.read = true;
    }
}
