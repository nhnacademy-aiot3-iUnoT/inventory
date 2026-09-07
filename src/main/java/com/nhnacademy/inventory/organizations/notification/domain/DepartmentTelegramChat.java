package com.nhnacademy.inventory.organizations.notification.domain;

import com.nhnacademy.inventory.organizations.department.domain.Department;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "department_telegram_chats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DepartmentTelegramChat {

    @Builder
    private DepartmentTelegramChat(Department department, String chatId, Boolean isEnabled) {
        this.department = department;
        this.chatId = chatId;
        this.isEnabled = isEnabled;
    }

    public static DepartmentTelegramChat create(Department department, String chatId, boolean enabled) {
        return DepartmentTelegramChat.builder()
                .department(department)
                .chatId(chatId)
                .isEnabled(enabled)
                .build();
    }

    public void update(String chatId, boolean enabled) {
        this.chatId = chatId;
        this.isEnabled = enabled;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_telegram_chat_id")
    private Long departmentTelegramChatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "chat_id", nullable = false, length = 255)
    private String chatId;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

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
