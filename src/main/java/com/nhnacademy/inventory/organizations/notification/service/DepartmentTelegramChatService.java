package com.nhnacademy.inventory.organizations.notification.service;

import com.nhnacademy.inventory.organizations.department.domain.Department;
import com.nhnacademy.inventory.organizations.department.exception.DepartmentNotFoundException;
import com.nhnacademy.inventory.organizations.department.repository.DepartmentRepository;
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
            Long organizationId,
            Long departmentId,
            DepartmentTelegramChatRegisterRequest request
    ) {
        organizationAccessService.requireOwnerOrBossOf(organizationId);

        Department department = resolveDepartment(organizationId, departmentId);
        boolean enabled = request.enabled() == null || request.enabled();

        // 같은 단톡방이 다른 부서에 물려있으면 거절한다.
        departmentTelegramChatRepository.findByChatId(request.chatId())
                .filter(linked -> !linked.getDepartment().getId().equals(departmentId))
                .ifPresent(linked -> {
                    throw new DepartmentTelegramChatAlreadyLinkedException();
                });

        DepartmentTelegramChat chat = departmentTelegramChatRepository
                .findByDepartmentId(departmentId)
                .map(existing -> {
                    existing.update(request.chatId(), enabled);
                    return existing;
                })
                .orElseGet(() -> departmentTelegramChatRepository.save(
                        DepartmentTelegramChat.create(department, request.chatId(), enabled)
                ));

        return DepartmentTelegramChatResponse.from(chat);
    }

    public DepartmentTelegramChatResponse getByDepartment(Long organizationId, Long departmentId) {
        organizationAccessService.requireOwnerOrBossOf(organizationId);
        resolveDepartment(organizationId, departmentId);

        return departmentTelegramChatRepository.findByDepartmentId(departmentId)
                .map(DepartmentTelegramChatResponse::from)
                .orElseThrow(DepartmentTelegramChatNotFoundException::new);
    }

    @Transactional
    public void unlink(Long organizationId, Long departmentId) {
        organizationAccessService.requireOwnerOrBossOf(organizationId);
        resolveDepartment(organizationId, departmentId);

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

    private Department resolveDepartment(Long organizationId, Long departmentId) {
        return departmentRepository.findByIdAndOrganizationId(departmentId, organizationId)
                .orElseThrow(DepartmentNotFoundException::new);
    }
}
