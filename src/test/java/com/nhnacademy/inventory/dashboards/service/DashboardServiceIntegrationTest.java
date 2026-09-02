package com.nhnacademy.inventory.dashboards.service;

import com.nhnacademy.inventory.dashboards.dto.DashboardDepartmentsResponse;
import com.nhnacademy.inventory.dashboards.dto.DashboardExpiringResponse;
import com.nhnacademy.inventory.dashboards.dto.DashboardSummaryResponse;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 대시보드 API가 실제 컨텍스트에서 동작하는지 확인한다.
 * departmentId 없이(조직 전체) 호출하는 경로까지 포함한다.
 */
@SpringBootTest
@Transactional
@Sql("/sql/inventory-test-data.sql")
class DashboardServiceIntegrationTest {

    private static final UUID OWNER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    DashboardSummaryService dashboardSummaryService;

    @Autowired
    DashboardExpiringService dashboardExpiringService;

    @Autowired
    DashboardDepartmentService dashboardDepartmentService;

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    OrganizationMemberRepository organizationMemberRepository;

    @BeforeEach
    void setUp() {
        Organization organization = organizationRepository.findById(1L).orElseThrow();

        organizationMemberRepository.save(
                OrganizationMember.createUser(organization, OWNER_UUID, OrganizationRole.ORG_OWNER));

        UserContext.setUserUuid(OWNER_UUID);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("관리자가 조직 전체(departmentId 없음)로 KPI를 조회할 수 있다.")
    void getSummary_OrgWide() {
        DashboardSummaryResponse summary =
                dashboardSummaryService.getSummary(null, LocalDate.now());

        assertAll(
                () -> assertNotNull(summary),
                () -> assertNotNull(summary.inbound()),
                () -> assertEquals(0L, summary.inbound().value()),
                () -> assertTrue(summary.expiring().value() >= 0L)
        );
    }

    @Test
    @DisplayName("관리자가 조직 전체(departmentId 없음)로 임박 목록을 조회할 수 있다.")
    void getExpiring_OrgWide() {
        DashboardExpiringResponse expiring =
                dashboardExpiringService.getExpiring(null, null, 5);

        assertAll(
                () -> assertNotNull(expiring),
                () -> assertNotNull(expiring.items()),
                () -> assertTrue(expiring.within30Count() >= 0L)
        );
    }

    @Test
    @DisplayName("부서를 지정해 임박 목록을 조회할 수 있다.")
    void getExpiring_ByDepartment() {
        DashboardExpiringResponse expiring =
                dashboardExpiringService.getExpiring(1L, 3650, 5);

        assertAll(
                () -> assertNotNull(expiring),
                // 조회 기간(3650일) 안에 드는 재고가 목록에 담긴다
                () -> assertFalse(expiring.items().isEmpty()),
                () -> assertTrue(expiring.items().stream()
                        .allMatch(item -> item.storageName() != null))
        );
    }

    @Test
    @DisplayName("부서 드롭다운 목록을 조회할 수 있다.")
    void getDepartmentOptions() {
        DashboardDepartmentsResponse departments =
                dashboardDepartmentService.getDepartmentOptions();

        assertAll(
                () -> assertTrue(departments.orgAdmin()),
                // 관리자는 조직의 모든 부서를 고를 수 있다
                () -> assertFalse(departments.departments().isEmpty()),
                () -> assertNotNull(departments.organizationName())
        );
    }
}
