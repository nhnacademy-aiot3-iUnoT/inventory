package com.nhnacademy.inventory.organizations.department.service;

import com.nhnacademy.inventory.organizations.department.domain.Department;
import com.nhnacademy.inventory.organizations.department.dto.request.DepartmentCreateRequest;
import com.nhnacademy.inventory.organizations.department.dto.request.DepartmentStatusUpdateRequest;
import com.nhnacademy.inventory.organizations.department.dto.request.DepartmentUpdateRequest;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentCreateResponse;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentInfoResponse;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentListResponse;
import com.nhnacademy.inventory.organizations.department.exception.DepartmentAlreadyExistsException;
import com.nhnacademy.inventory.organizations.department.exception.DepartmentNotFoundException;
import com.nhnacademy.inventory.organizations.department.repository.DepartmentRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final OrganizationAccessService orgAccessService;

    @Transactional
    public DepartmentCreateResponse createDepartment(DepartmentCreateRequest request) {
        Organization organization = orgAccessService.requireOwnerOrBossOrganization();

        if (departmentRepository.existsByOrganizationIdAndName(organization.getId(), request.name())) {
            throw new DepartmentAlreadyExistsException();
        }

        Department department = Department.create(organization, request.name(), request.description());

        Department savedDepartment = departmentRepository.save(department);

        log.info("부서({}) : {} 생성", savedDepartment.getId(), savedDepartment.getName());
        return DepartmentCreateResponse.from(savedDepartment);
    }

    public List<DepartmentListResponse> getDepartments() {
        Organization organization = orgAccessService.getCurrentMember().getOrganization();

        return departmentRepository
                .findAllByOrganizationId(organization.getId())
                .stream()
                .map(DepartmentListResponse::from)
                .toList();
    }

    public DepartmentInfoResponse getDepartment(Long departmentId) {
        Organization organization = orgAccessService.getCurrentMember().getOrganization();

        Department department = getDepartmentById(departmentId, organization.getId());

        return DepartmentInfoResponse.from(department);
    }

    public Department getDepartmentById(Long departmentId, Long organizationId) {
        return departmentRepository.findByIdAndOrganizationId(departmentId, organizationId)
                .orElseThrow(DepartmentNotFoundException::new);
    }

    @Transactional
    public DepartmentInfoResponse updateDepartmentStatus(DepartmentStatusUpdateRequest request, Long departmentId) {
        Organization organization = orgAccessService.requireOwnerOrBossOrganization();

        Department department = getDepartmentById(departmentId, organization.getId());

        department.updateStatus(request.status());

        return DepartmentInfoResponse.from(department);
    }

    @Transactional
    public DepartmentInfoResponse updateDepartment(DepartmentUpdateRequest request, Long departmentId) {
        Organization organization = orgAccessService.requireOwnerOrBossOrganization();

        Department department = getDepartmentById(departmentId, organization.getId());

        if (departmentRepository.existsByOrganizationIdAndNameAndIdNot(organization.getId(), request.name(), departmentId)) {
            throw new DepartmentAlreadyExistsException();
        }

        department.update(request.name(), request.description());

        return DepartmentInfoResponse.from(department);
    }

    @Transactional
    public void deleteDepartment(Long departmentId) {
        Organization organization = orgAccessService.requireOwnerOrBossOrganization();

        Department department = getDepartmentById(departmentId, organization.getId());

        departmentRepository.delete(department);
    }
}
