package com.nhnacademy.inventory.dashboards.service;

import com.nhnacademy.inventory.dashboards.exception.DepartmentAccessDeniedException;
import com.nhnacademy.inventory.dashboards.exception.OrgWideAccessDeniedException;
import com.nhnacademy.inventory.dashboards.exception.StorageAccessDeniedException;
import com.nhnacademy.inventory.organizations.department.dto.response.StorageByDepartmentResponse;
import com.nhnacademy.inventory.organizations.department.service.MemberDepartmentService;
import com.nhnacademy.inventory.organizations.department.service.StorageDepartmentService;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardScopeResolver {

    private final OrganizationAccessService orgAccessService;
    private final MemberDepartmentService memberDepartmentService;
    private final StorageDepartmentService storageDepartmentService;
    private final StorageService storageService;

    // 부서의 담당 저장소 ID 목록을 반환한다
    public List<Long> resolveStorageIds(Long departmentId) {
        if (departmentId == null) {
            if (!isOrgAdmin()) {
                throw new OrgWideAccessDeniedException();
            }

            return storageService.getStorageIds();
        }

        if (!memberDepartmentService.isMyDepartment(departmentId) && !isOrgAdmin()) {
            throw new DepartmentAccessDeniedException();
        }

        return storageDepartmentService.getStoragesByDepartmentId(departmentId)
                .stream()
                .map(StorageByDepartmentResponse::storageId)
                .toList();
    }

    // 저장소 단위 조회의 권한을 검증하고 그 저장소를 반환한다
    public Storage resolveStorage(Long storageId) {
        Storage storage = storageService.validateMemberAndGetStorage(storageId);

        if (!isOrgAdmin() && !memberDepartmentService.getAccessibleStorageIds().contains(storageId)) {
            throw new StorageAccessDeniedException();
        }

        return storage;
    }

    // 조직 관리자 인지 검증
    private boolean isOrgAdmin() {
        OrganizationMember member = orgAccessService.getCurrentMember();

        return member.isOwner() || member.isBoss();
    }
}
