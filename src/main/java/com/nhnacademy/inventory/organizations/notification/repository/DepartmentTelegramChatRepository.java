package com.nhnacademy.inventory.organizations.notification.repository;

import com.nhnacademy.inventory.organizations.notification.domain.DepartmentTelegramChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentTelegramChatRepository extends JpaRepository<DepartmentTelegramChat, Long> {

    // 챗봇 역조회: 단톡방 -> 부서
    Optional<DepartmentTelegramChat> findByChatIdAndIsEnabledTrue(String chatId);

    // 등록 시 다른 부서에 물려있는지 확인
    Optional<DepartmentTelegramChat> findByChatId(String chatId);

    Optional<DepartmentTelegramChat> findByDepartmentId(Long departmentId);
}
