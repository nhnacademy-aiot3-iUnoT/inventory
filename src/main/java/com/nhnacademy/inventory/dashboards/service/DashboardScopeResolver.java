package com.nhnacademy.inventory.dashboards.service;

import com.nhnacademy.inventory.dashboards.exception.DepartmentAccessDeniedException;
import com.nhnacademy.inventory.dashboards.exception.OrgWideAccessDeniedException;
import com.nhnacademy.inventory.dashboards.exception.StorageAccessDeniedException;
import com.nhnacademy.inventory.organizations.department.repository.MemberDepartmentRepository;
import com.nhnacademy.inventory.organizations.department.repository.StorageDepartmentRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import com.nhnacademy.inventory.organizations.storage.repository.StorageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 대시보드가 집계할 범위(저장소 ID 목록)를 구하고 접근 권한을 검증한다.
 * 대시보드 API 4개가 모두 이 클래스를 거쳐 범위를 얻는다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardScopeResolver {

    private final OrganizationAccessService orgAccessService;
    private final MemberDepartmentRepository memberDepartmentRepository;
    private final StorageDepartmentRepository storageDepartmentRepository;
    private final StorageRepository storageRepository;

    /**
     * 부서의 담당 저장소 ID 목록을 반환한다.
     * departmentId가 null이면 조직 전체 저장소를 반환하며, 이 경우 관리자만 허용한다.
     */
    public List<Long> resolveStorageIds(Long departmentId) {
        OrganizationMember member = orgAccessService.getCurrentMember();

        if (departmentId == null) {
            if (!isOrgAdmin(member)) {
                throw new OrgWideAccessDeniedException();
            }

            return storageRepository.findIdsByOrganizationIdAndStatusNot(
                    member.getOrganization().getId(), StorageStatus.CLOSED);
        }

        verifyDepartmentAccess(member, departmentId);

        return storageDepartmentRepository.findAllWithStorageByDepartmentId(departmentId)
                .stream()
                .map(sd -> sd.getStorage().getId())
                .toList();
    }

    /**
     * 저장소 단위 조회(환경 현황) 권한을 검증한다.
     * 내 부서에 배정된 저장소이거나 관리자여야 한다.
     */
    public void verifyStorageAccess(Long storageId) {
        OrganizationMember member = orgAccessService.getCurrentMember();

        if (isOrgAdmin(member)) {
            storageRepository.findByIdAndOrganization(storageId, member.getOrganization())
                    .orElseThrow(StorageAccessDeniedException::new);
            return;
        }

        boolean accessible = memberDepartmentRepository.findAccessibleStorageIds(member.getId())
                .contains(storageId);

        if (!accessible) {
            throw new StorageAccessDeniedException();
        }
    }

    public OrganizationMember getCurrentMember() {
        return orgAccessService.getCurrentMember();
    }

    private void verifyDepartmentAccess(OrganizationMember member, Long departmentId) {
        boolean mine = memberDepartmentRepository
                .existsByDepartmentIdAndOrganizationMemberId(departmentId, member.getId());

        if (!mine && !isOrgAdmin(member)) {
            throw new DepartmentAccessDeniedException();
        }
    }

    private boolean isOrgAdmin(OrganizationMember member) {
        return member.isOwner() || member.isBoss();
    }
}
