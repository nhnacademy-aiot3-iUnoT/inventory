package com.nhnacademy.inventory.organizations.department.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.department.domain.Department;
import com.nhnacademy.inventory.organizations.department.domain.StorageDepartment;
import com.nhnacademy.inventory.organizations.department.exception.DepartmentNotFoundException;
import com.nhnacademy.inventory.organizations.department.repository.StorageDepartmentRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import com.nhnacademy.inventory.organizations.storage.exception.StorageNotFoundException;
import com.nhnacademy.inventory.organizations.storage.repository.StorageRepository;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageDepartmentServiceTest {
    @Mock
    private StorageDepartmentRepository storageDepartmentRepository;

    @Mock private StorageRepository storageRepository;

    @Mock private DepartmentService departmentService;
    @Mock private StorageService storageService;
    @Mock private OrganizationAccessService orgAccessService;
    @InjectMocks private StorageDepartmentService service;

    private Organization organization;
    private Department department;
    private Storage storage;
    private StorageDepartment relation;

    @BeforeEach
    void setUp() {
        organization = TestFixtures.createOrganization("테스트 조직", "1234567890");
        ReflectionTestUtils.setField(organization, "id", 1L);
        department = Department.create(organization, "이비인후과", "설명");
        ReflectionTestUtils.setField(department, "id", 1L);
        storage = TestFixtures.createStorage(organization, "본관 저장소");
        ReflectionTestUtils.setField(storage, "id", 1L);
        relation = StorageDepartment.create(storage, department);
    }

    @Nested
    @DisplayName("부서별 저장소 조회")
    class GetStoragesByDepartment {
        @Test
        @DisplayName("성공")
        void success() {
            given(storageDepartmentRepository.findAllByDepartmentId(1L)).willReturn(List.of(relation));

            var result = service.getStoragesByDepartmentId(1L);

            assertEquals(1, result.size());
            assertEquals("본관 저장소", result.getFirst().name());
            assertEquals(StorageStatus.ACTIVE, result.getFirst().storageStatus());
            verify(departmentService).getDepartment(1L);
        }

        @Test
        @DisplayName("성공 - 결과 없음")
        void empty() {
            given(storageDepartmentRepository.findAllByDepartmentId(1L)).willReturn(List.of());

            assertEquals(0, service.getStoragesByDepartmentId(1L).size());
        }

        @Test
        @DisplayName("실패 - 부서 없음")
        void departmentNotFound() {
            given(departmentService.getDepartment(1L))
                    .willThrow(new DepartmentNotFoundException());

            assertThrows(DepartmentNotFoundException.class,
                    () -> service.getStoragesByDepartmentId(1L));
            verify(storageDepartmentRepository, never()).findAllByDepartmentId(1L);
        }
    }

    @Nested
    @DisplayName("저장소별 부서 조회")
    class GetDepartmentsByStorage {
        @Test
        @DisplayName("성공")
        void success() {
            OrganizationMember member = TestFixtures.createOrganizationMember(organization);
            given(orgAccessService.getCurrentMember()).willReturn(member);
            given(storageRepository.findByIdAndOrganization(1L, organization)).willReturn(Optional.of(storage));
            given(storageDepartmentRepository.findAllByStorageId(1L)).willReturn(List.of(relation));

            var result = service.getDepartmentsByStorageId(1L);

            assertEquals(1, result.size());
            assertEquals("이비인후과", result.getFirst().name());
        }

        @Test
        @DisplayName("성공 - 결과 없음")
        void empty() {
            OrganizationMember member = TestFixtures.createOrganizationMember(organization);
            given(orgAccessService.getCurrentMember()).willReturn(member);
            given(storageRepository.findByIdAndOrganization(1L, organization)).willReturn(Optional.of(storage));
            given(storageDepartmentRepository.findAllByStorageId(1L)).willReturn(List.of());

            assertEquals(0, service.getDepartmentsByStorageId(1L).size());
        }

        @Test
        @DisplayName("실패 - 저장소 없음")
        void storageNotFound() {
            OrganizationMember member = TestFixtures.createOrganizationMember(organization);
            given(orgAccessService.getCurrentMember()).willReturn(member);
            given(storageRepository.findByIdAndOrganization(1L, organization)).willReturn(Optional.empty());

            assertThrows(StorageNotFoundException.class,
                    () -> service.getDepartmentsByStorageId(1L));
            verify(storageDepartmentRepository, never()).findAllByStorageId(1L);
        }
    }

    @Nested
    @DisplayName("저장소 연결")
    class AddStorage {
        @Test
        @DisplayName("성공")
        void success() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willReturn(organization);
            given(departmentService.getDepartmentById(1L, 1L)).willReturn(department);
            given(storageService.validateOwnerAndGetStorage(1L)).willReturn(storage);
            given(storageDepartmentRepository.existsByDepartmentIdAndStorageId(1L, 1L)).willReturn(false);

            service.addStorage(1L, 1L);

            verify(storageDepartmentRepository).save(any(StorageDepartment.class));
        }

        @Test
        @DisplayName("성공 - 이미 연결된 저장소")
        void alreadyExists() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willReturn(organization);
            given(departmentService.getDepartmentById(1L, 1L)).willReturn(department);
            given(storageService.validateOwnerAndGetStorage(1L)).willReturn(storage);
            given(storageDepartmentRepository.existsByDepartmentIdAndStorageId(1L, 1L)).willReturn(true);

            service.addStorage(1L, 1L);

            verify(storageDepartmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() {
            given(orgAccessService.requireOwnerOrBossOrganization())
                    .willThrow(new ForbiddenException());

            assertThrows(ForbiddenException.class, () -> service.addStorage(1L, 1L));
            verify(storageDepartmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("저장소 연결 해제")
    class RemoveStorage {
        @Test
        @DisplayName("성공")
        void success() {
            given(storageService.validateOwnerAndGetStorage(1L)).willReturn(storage);

            service.removeStorage(1L, 1L);

            verify(storageDepartmentRepository).deleteByDepartmentIdAndStorageId(1L, 1L);
        }

        @Test
        @DisplayName("실패 - 저장소 없음")
        void storageNotFound() {
            given(storageService.validateOwnerAndGetStorage(1L))
                    .willThrow(new StorageNotFoundException());

            assertThrows(StorageNotFoundException.class, () -> service.removeStorage(1L, 1L));
            verify(storageDepartmentRepository, never()).deleteByDepartmentIdAndStorageId(1L, 1L);
        }
    }
}
