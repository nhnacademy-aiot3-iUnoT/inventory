package com.nhnacademy.inventory.inventories.alert.repository;

import com.nhnacademy.inventory.global.config.QuerydslConfig;
import com.nhnacademy.inventory.inventories.alert.domain.AlertType;
import com.nhnacademy.inventory.inventories.alert.dto.AlertInfoResponse;
import com.nhnacademy.inventory.inventories.alert.dto.AlertSearchCondition;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.repository.OrganizationRepository;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(QuerydslConfig.class)
class AlertRepositoryTest {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private Organization organization;
    @BeforeEach
    void setUp() {
        organization = organizationRepository.save(
                TestFixtures.createOrganization("테스트 조직1", "1234567890")
        );

        alertRepository.saveAll(
                List.of(
                        TestFixtures.createAlert(organization, AlertType.LOW_STOCK, "메시지1", true),
                        TestFixtures.createAlert(organization, AlertType.LOW_STOCK, "메시지2", false),
                        TestFixtures.createAlert(organization, AlertType.ENV_WARNING, "메시지3", true),
                        TestFixtures.createAlert(organization, AlertType.ENV_WARNING, "메시지4", false),
                        TestFixtures.createAlert(organization, AlertType.EXPIRING, "메시지5", true),
                        TestFixtures.createAlert(organization, AlertType.EXPIRING, "메시지6", false)
                )
        );
    }

    @Nested
    @DisplayName("QueryDsl - SearchByCondition 테스트")
    class SearchByConditionTest{

        @Test
        @DisplayName("조건(isChecked, alertType)이 모두 null일때 전체 조회")
        void search_ConditionIsNull(){
            AlertSearchCondition condition = new AlertSearchCondition(null, null);
            Pageable pageable = PageRequest.of(0, 10);

            Page<AlertInfoResponse> result = alertRepository.searchByCondition(organization, condition, pageable);

            List<AlertInfoResponse> actual = result.getContent();

            assertEquals(6, actual.size());
        }

        @Test
        @DisplayName("isChecked 조건 조회")
        void search_IsRead(){
            AlertSearchCondition condition = new AlertSearchCondition(null, true);
            Pageable pageable = PageRequest.of(0, 10);

            Page<AlertInfoResponse> result = alertRepository.searchByCondition(organization, condition, pageable);

            List<AlertInfoResponse> actual = result.getContent();

            assertAll(
                    () -> assertEquals(3, actual.size()),
                    () -> assertEquals(true, actual.getFirst().isChecked()),
                    () -> assertEquals(true, actual.get(1).isChecked()),
                    () -> assertEquals(true, actual.getLast().isChecked())
            );
        }

        @Test
        @DisplayName("alertType 조건 조회")
        void search_AlertType(){
            AlertSearchCondition condition = new AlertSearchCondition(AlertType.LOW_STOCK, null);
            Pageable pageable = PageRequest.of(0, 10);

            Page<AlertInfoResponse> result = alertRepository.searchByCondition(organization, condition, pageable);

            List<AlertInfoResponse> actual = result.getContent();

            assertAll(
                    () -> assertEquals(2, actual.size()),
                    () -> assertEquals(AlertType.LOW_STOCK, actual.getFirst().alertType()),
                    () -> assertEquals(AlertType.LOW_STOCK, actual.getLast().alertType())
            );
        }

        @Test
        @DisplayName("조건(isChecked, alertType) 조회")
        void search_Condition(){
            AlertSearchCondition condition = new AlertSearchCondition(AlertType.EXPIRING, true);
            Pageable pageable = PageRequest.of(0, 10);

            Page<AlertInfoResponse> result = alertRepository.searchByCondition(organization, condition, pageable);

            List<AlertInfoResponse> actual = result.getContent();

            assertAll(
                    () -> assertEquals(1, actual.size()),
                    () -> assertEquals(true, actual.getFirst().isChecked()),
                    () -> assertEquals(AlertType.EXPIRING, actual.getFirst().alertType())
            );
        }
    }
}