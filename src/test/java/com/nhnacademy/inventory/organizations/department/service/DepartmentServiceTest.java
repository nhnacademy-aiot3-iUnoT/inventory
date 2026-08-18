package com.nhnacademy.inventory.organizations.department.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.department.domain.Department;
import com.nhnacademy.inventory.organizations.department.domain.DepartmentStatus;
import com.nhnacademy.inventory.organizations.department.dto.request.DepartmentCreateRequest;
import com.nhnacademy.inventory.organizations.department.dto.request.DepartmentStatusUpdateRequest;
import com.nhnacademy.inventory.organizations.department.dto.request.DepartmentUpdateRequest;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentCreateResponse;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentInfoResponse;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentListResponse;
import com.nhnacademy.inventory.organizations.department.exception.DepartmentAlreadyExistsException;
import com.nhnacademy.inventory.organizations.department.exception.DepartmentNotFoundException;
import com.nhnacademy.inventory.organizations.department.repository.DepartmentRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private OrganizationAccessService orgAccessService;

    @InjectMocks
    private DepartmentService departmentService;

    private Organization organization;
    private OrganizationMember member;
    private Department department;

    @BeforeEach
    void setUp() {
        organization = TestFixtures.createOrganization("테스트 조직", "1234567890");
        ReflectionTestUtils.setField(organization, "id", 1L);

        member = TestFixtures.createOrganizationMember(organization);

        department = Department.create(organization, "이비인후과", "부서 설명");
        ReflectionTestUtils.setField(department, "id", 1L);
    }

    @Nested
    @DisplayName("부서 생성")
    class create_department {

        @Test
        @DisplayName("성공")
        void success() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willReturn(organization);
            given(departmentRepository.existsByOrganizationIdAndName(anyLong(), anyString())).willReturn(false);
            given(departmentRepository.save(any(Department.class))).willReturn(department);

            DepartmentCreateRequest request = new DepartmentCreateRequest("이비인후과", "부서 설명");

            DepartmentCreateResponse response = departmentService.createDepartment(request);

            assertEquals("이비인후과", response.name());
            verify(orgAccessService).requireOwnerOrBossOrganization();
            verify(departmentRepository).save(any(Department.class));
        }

        @Test
        @DisplayName("성공 - 부서 설명 없음")
        void success_description_blank() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willReturn(organization);
            given(departmentRepository.existsByOrganizationIdAndName(anyLong(), anyString())).willReturn(false);
            given(departmentRepository.save(any(Department.class))).willReturn(department);

            DepartmentCreateRequest request = new DepartmentCreateRequest("이비인후과", null);

            DepartmentCreateResponse response = departmentService.createDepartment(request);

            assertEquals("이비인후과", response.name());
            verify(orgAccessService).requireOwnerOrBossOrganization();
            verify(departmentRepository).save(any(Department.class));
        }

        @Test
        @DisplayName("실패 - BOSS, OWNER 아님")
        void forbidden() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willThrow(new ForbiddenException());

            DepartmentCreateRequest request = new DepartmentCreateRequest("이비인후과", "부서 설명");

            assertThrows(ForbiddenException.class, () -> departmentService.createDepartment(request));

            verify(departmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("부서 생성 실패 - 부서 이름 중복")
        void duplicate() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willReturn(organization);
            given(departmentRepository.existsByOrganizationIdAndName(anyLong(), anyString())).willReturn(true);

            DepartmentCreateRequest request = new DepartmentCreateRequest("이비인후과", "부서 설명");

            assertThrows(DepartmentAlreadyExistsException.class, () -> departmentService.createDepartment(request));

            verify(departmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("부서 목록 조회")
    class get_departments {

        @Test
        @DisplayName("성공")
        void success() {
            given(orgAccessService.getCurrentMember()).willReturn(member);
            given(departmentRepository.findAllByOrganizationId(anyLong())).willReturn(List.of(department));

            List<DepartmentListResponse> responses = departmentService.getDepartments();

            assertEquals(1, responses.size());
            assertEquals("이비인후과", responses.getFirst().name());
            assertEquals(DepartmentStatus.ACTIVE, responses.getFirst().status());
            verify(orgAccessService).getCurrentMember();
            verify(departmentRepository).findAllByOrganizationId(organization.getId());
        }

        @Test
        @DisplayName("성공 - 조회 결과 없음")
        void success_empty() {
            given(orgAccessService.getCurrentMember()).willReturn(member);
            given(departmentRepository.findAllByOrganizationId(anyLong())).willReturn(List.of());

            List<DepartmentListResponse> responses = departmentService.getDepartments();

            assertTrue(responses.isEmpty());
            verify(orgAccessService).getCurrentMember();
        }
    }

    @Nested
    @DisplayName("부서 단건 조회")
    class get_department {

        @Test
        @DisplayName("성공")
        void success() {
            given(orgAccessService.getCurrentMember()).willReturn(member);
            given(departmentRepository.findByIdAndOrganizationId(department.getId(), organization.getId())).willReturn(Optional.of(department));

            DepartmentInfoResponse response = departmentService.getDepartment(department.getId());

            assertEquals("이비인후과", response.name());
            verify(orgAccessService).getCurrentMember();
            verify(departmentRepository).findByIdAndOrganizationId(department.getId(), organization.getId());
        }

        @Test
        @DisplayName("실패 - 부서 없음")
        void notFound() {
            given(orgAccessService.getCurrentMember()).willReturn(member);
            given(departmentRepository.findByIdAndOrganizationId(1L, organization.getId())).willReturn(Optional.empty());

            assertThrows(DepartmentNotFoundException.class, () -> departmentService.getDepartment(1L));

            verify(orgAccessService).getCurrentMember();
            verify(departmentRepository).findByIdAndOrganizationId(1L, organization.getId());
        }
    }

    @Nested
    @DisplayName("부서 수정")
    class update_department {

        @Test
        @DisplayName("부서 상태 변경 성공")
        void status_success() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willReturn(organization);
            given(departmentRepository.findByIdAndOrganizationId(department.getId(), organization.getId())).willReturn(Optional.of(department));

            DepartmentStatusUpdateRequest request = new DepartmentStatusUpdateRequest(DepartmentStatus.INACTIVE);

            DepartmentInfoResponse response = departmentService.updateDepartmentStatus(request, department.getId());

            assertEquals(DepartmentStatus.INACTIVE, response.status());
            assertEquals(DepartmentStatus.INACTIVE, department.getStatus());
            verify(orgAccessService).requireOwnerOrBossOrganization();
        }

        @Test
        @DisplayName("부서 상태 변경 실패 - 권한 없음")
        void status_forbidden() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willThrow(new ForbiddenException());

            DepartmentStatusUpdateRequest request = new DepartmentStatusUpdateRequest(DepartmentStatus.INACTIVE);

            assertThrows(ForbiddenException.class, () -> departmentService.updateDepartmentStatus(request, department.getId()));

            verify(departmentRepository, never()).findByIdAndOrganizationId(anyLong(), anyLong());
        }

        @Test
        @DisplayName("부서 상태 변경 실패 - 부서 없음")
        void status_not_found() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willReturn(organization);
            given(departmentRepository.findByIdAndOrganizationId(anyLong(), anyLong())).willReturn(Optional.empty());

            DepartmentStatusUpdateRequest request = new DepartmentStatusUpdateRequest(DepartmentStatus.INACTIVE);

            assertThrows(DepartmentNotFoundException.class, () -> departmentService.updateDepartmentStatus(request, 999L));
        }

        @Test
        @DisplayName("부서 정보 수정 성공")
        void info_success() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willReturn(organization);
            given(departmentRepository.findByIdAndOrganizationId(anyLong(), anyLong())).willReturn(Optional.of(department));
            given(departmentRepository.existsByOrganizationIdAndNameAndIdNot(anyLong(), anyString(), anyLong())).willReturn(false);

            DepartmentUpdateRequest request = new DepartmentUpdateRequest("기획팀", "기획 부서");

            DepartmentInfoResponse response = departmentService.updateDepartment(request, department.getId());

            assertEquals("기획팀", response.name());
            assertEquals("기획 부서", response.description());
        }

        @Test
        @DisplayName("부서 정보 수정 실패 - 권한 없음")
        void info_forbidden() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willThrow(new ForbiddenException());

            DepartmentUpdateRequest request = new DepartmentUpdateRequest("기획팀", "기획 부서");

            assertThrows(ForbiddenException.class, () -> departmentService.updateDepartment(request, department.getId()));

            verify(departmentRepository, never()).findByIdAndOrganizationId(anyLong(), anyLong());
        }

        @Test
        @DisplayName("부서 정보 수정 실패 - 부서 없음")
        void info_notFound() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willReturn(organization);
            given(departmentRepository.findByIdAndOrganizationId(anyLong(), anyLong())).willReturn(Optional.empty());

            DepartmentUpdateRequest request = new DepartmentUpdateRequest("기획팀", "기획 부서");

            assertThrows(DepartmentNotFoundException.class, () -> departmentService.updateDepartment(request, department.getId()));
        }

        @Test
        @DisplayName("부서 정보 수정 실패 - 부서 이름 중복")
        void info_duplicate() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willReturn(organization);
            given(departmentRepository.findByIdAndOrganizationId(anyLong(), anyLong())).willReturn(Optional.of(department));
            given(departmentRepository.existsByOrganizationIdAndNameAndIdNot(anyLong(), anyString(), anyLong())).willReturn(true);

            DepartmentUpdateRequest request = new DepartmentUpdateRequest("기획팀", "기획 부서");

            assertThrows(DepartmentAlreadyExistsException.class, () -> departmentService.updateDepartment(request, department.getId()));
        }
    }

    @Nested
    @DisplayName("부서 삭제")
    class delete_department {

        @Test
        @DisplayName("성공")
        void success() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willReturn(organization);
            given(departmentRepository.findByIdAndOrganizationId(anyLong(), anyLong())).willReturn(Optional.of(department));

            departmentService.deleteDepartment(department.getId());

            verify(orgAccessService).requireOwnerOrBossOrganization();
            verify(departmentRepository).delete(department);
        }

        @Test
        @DisplayName("부서 삭제 실패 - 권한 없음")
        void forbidden() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willThrow(new ForbiddenException());

            assertThrows(ForbiddenException.class, () -> departmentService.deleteDepartment(department.getId()));

            verify(departmentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("부서 삭제 실패 - 부서 없음")
        void notFound() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willReturn(organization);
            given(departmentRepository.findByIdAndOrganizationId(anyLong(), anyLong())).willReturn(Optional.empty());

            assertThrows(DepartmentNotFoundException.class, () -> departmentService.deleteDepartment(department.getId()));

            verify(departmentRepository, never()).delete(any());
        }
    }
}
