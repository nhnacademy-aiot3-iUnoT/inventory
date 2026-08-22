package com.nhnacademy.inventory.organizations.department.service;

import com.nhnacademy.inventory.organizations.department.controller.DepartmentByStorageResponse;
import com.nhnacademy.inventory.organizations.department.repository.StorageDepartmentRepository;
import com.nhnacademy.inventory.organizations.department.domain.Department;
import com.nhnacademy.inventory.organizations.department.domain.StorageDepartment;
import com.nhnacademy.inventory.organizations.department.dto.response.StorageByDepartmentResponse;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.exception.StorageNotFoundException;
import com.nhnacademy.inventory.organizations.storage.repository.StorageRepository;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StorageDepartmentService {
    private final StorageDepartmentRepository storageDepartmentRepository;
    private final StorageRepository storageRepository;

    private final DepartmentService departmentService ;
    private final StorageService storageService;
    private final OrganizationAccessService orgAccessService;

    public List<StorageByDepartmentResponse> getStoragesByDepartmentId(Long departmentId) {
        departmentService.getDepartment(departmentId);

        return storageDepartmentRepository.findAllByDepartmentId(departmentId).stream()
                .map(sd -> new StorageByDepartmentResponse(
                        sd.getStorage().getId(),
                        sd.getStorage().getName(),
                        sd.getStorage().getStatus()))
                .toList();
    }

    public List<DepartmentByStorageResponse> getDepartmentsByStorageId(Long storageId) {
        Organization organization = orgAccessService.getCurrentMember().getOrganization();

        storageRepository.findByIdAndOrganization(storageId, organization)
                .orElseThrow(StorageNotFoundException::new);

        return storageDepartmentRepository.findAllByStorageId(storageId).stream()
                .map(sd -> new DepartmentByStorageResponse(
                        sd.getDepartment().getId(),
                        sd.getDepartment().getName(),
                        sd.getDepartment().getStatus()))
                .toList();
    }


    @Transactional
    public void addStorage(Long departmentId, Long storageId) {
        Organization organization = orgAccessService.requireOwnerOrBossOrganization();

        Department department = departmentService.getDepartmentById(departmentId, organization.getId());

        Storage storage = storageService.validateOwnerAndGetStorage(storageId);

        if (!storageDepartmentRepository.existsByDepartmentIdAndStorageId(departmentId, storageId)) {
            storageDepartmentRepository.save(StorageDepartment.create(storage, department));
        }

    }

    @Transactional
    public void removeStorage(Long departmentId, Long storageId) {
        departmentService.getDepartment(departmentId);

        storageService.validateOwnerAndGetStorage(storageId);
        storageDepartmentRepository.deleteByDepartmentIdAndStorageId(departmentId, storageId);
    }

}
