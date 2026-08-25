package com.nhnacademy.inventory.inventories.alert.domain;

import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", length = 30, nullable = false)
    private AlertType alertType;

    @Column(name = "message", length = 255, nullable = false)
    private String message;

    @Column(name = "is_checked", nullable = false)
    private Boolean isChecked;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Alert(Organization organization, AlertType alertType, String message, Boolean isChecked) {
        this.organization = organization;
        this.alertType = alertType;
        this.message = message;
        this.isChecked = isChecked;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void markAsChecked(){
        this.isChecked = true;
    }
}

