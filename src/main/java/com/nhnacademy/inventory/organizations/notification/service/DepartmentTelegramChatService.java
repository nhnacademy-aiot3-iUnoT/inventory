package com.nhnacademy.inventory.organizations.notification.service;

import com.nhnacademy.inventory.organizations.department.domain.Department;
import com.nhnacademy.inventory.organizations.department.exception.DepartmentNotFoundException;
import com.nhnacademy.inventory.organizations.department.repository.DepartmentRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.notification.domain.DepartmentTelegramChat;
import com.nhnacademy.inventory.organizations.notification.dto.request.DepartmentTelegramChatRegisterRequest;
import com.nhnacademy.inventory.organizations.notification.dto.response.DepartmentTelegramChatResponse;
import com.nhnacademy.inventory.organizations.notification.exception.DepartmentTelegramChatAlreadyLinkedException;
import com.nhnacademy.inventory.organizations.notification.exception.DepartmentTelegramChatNotFoundException;
import com.nhnacademy.inventory.organizations.notification.repository.DepartmentTelegramChatRepository;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentTelegramChatService {

    private final DepartmentTelegramChatRepository departmentTelegramChatRepository;
    private final DepartmentRepository departmentRepository;
    private final OrganizationAccessService organizationAccessService;

    /**
     * 부서에 단톡방을 연결한다. 이미 연결된 부서면 chat id를 교체한다.
     */
    @Transactional
    public DepartmentTelegramChatResponse register(
            Long departmentId,
            DepartmentTelegramChatRegisterRequest request
    ) {
        Department department = resolveDepartment(departmentId);
        boolean enabled = request.enabled() == null || request.enabled();
        String chatId = request.chatId().trim();

        // 같은 단톡방이 다른 부서에 물려있으면 거절한다. 비활성 연결도 UNIQUE 제약에 걸리므로 함께 확인한다.
        departmentTelegramChatRepository.findByChatId(chatId)
                .filter(linked -> !linked.getDepartment().getId().equals(departmentId))
                .ifPresent(linked -> {
                    throw new DepartmentTelegramChatAlreadyLinkedException();
                });

        DepartmentTelegramChat chat = departmentTelegramChatRepository
                .findByDepartmentId(departmentId)
                .map(existing -> {
                    existing.update(chatId, enabled);
                    return existing;
                })
                .orElseGet(() -> departmentTelegramChatRepository.save(
                        DepartmentTelegramChat.create(department, chatId, enabled)
                ));

        return DepartmentTelegramChatResponse.from(chat);
    }

    /**
     * 부서 설정 화면에서 쓴다. 연결이 없으면 null을 돌려준다.
     */
    public DepartmentTelegramChatResponse getByDepartment(Long departmentId) {
        resolveDepartment(departmentId);

        return departmentTelegramChatRepository.findByDepartmentId(departmentId)
                .map(DepartmentTelegramChatResponse::from)
                .orElse(null);
    }

    @Transactional
    public void unlink(Long departmentId) {
        resolveDepartment(departmentId);

        DepartmentTelegramChat chat = departmentTelegramChatRepository.findByDepartmentId(departmentId)
                .orElseThrow(DepartmentTelegramChatNotFoundException::new);

        departmentTelegramChatRepository.delete(chat);
    }

    /**
     * 챗봇이 단톡방 chat id로 부서를 역조회할 때 쓴다. 텔레그램 수신 경로에서 호출되므로 조직원 인증을 거치지 않는다.
     */
    public Department resolveDepartmentByChatId(String chatId) {
        return departmentTelegramChatRepository.findByChatIdAndIsEnabledTrue(chatId)
                .map(DepartmentTelegramChat::getDepartment)
                .orElseThrow(DepartmentTelegramChatNotFoundException::new);
    }

    /**
     * 권한을 확인하고, 내 조직의 부서인지까지 검증한다.
     */
    private Department resolveDepartment(Long departmentId) {
        OrganizationMember member = organizationAccessService.requireOwnerOrBoss();

        return departmentRepository
                .findByIdAndOrganizationId(departmentId, member.getOrganization().getId())
                .orElseThrow(DepartmentNotFoundException::new);
    }
}
