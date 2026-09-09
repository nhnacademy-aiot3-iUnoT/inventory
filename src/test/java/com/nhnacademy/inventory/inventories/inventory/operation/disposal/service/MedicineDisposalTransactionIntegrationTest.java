package com.nhnacademy.inventory.inventories.inventory.operation.disposal.service;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.exception.InvalidExpiredDisposalTargetException;
import com.nhnacademy.inventory.inventories.inventory.operation.InventoryOperationAccessValidator;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.domain.DisposalOperation;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto.ExpiredInventoryDisposalRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto.MedicineDisposalRequest;
import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@Sql("/sql/inventory-test-data.sql")
class MedicineDisposalTransactionIntegrationTest {

    @Autowired
    private MedicineDisposalService medicineDisposalService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private InventoryOperationAccessValidator accessValidator;

    @MockitoBean
    private DisposalOperation disposalOperation;

    @BeforeEach
    void setUp() {
        Date expiredDate =
                Date.valueOf(LocalDate.now().minusDays(1));

        jdbcTemplate.update(
                """
                update medicine_inventorys
                set expiration_date = ?,
                    current_quantity = 3,
                    management_status = 'NORMAL'
                where inventory_id = 1
                """,
                expiredDate
        );

        jdbcTemplate.update(
                """
                update medicine_inventorys
                set expiration_date = ?,
                    current_quantity = 5,
                    management_status = 'NORMAL'
                where inventory_id = 2
                """,
                expiredDate
        );

        doNothing()
                .when(accessValidator)
                .validate(
                        any(Zone.class),
                        any(Medicine.class)
                );

        doAnswer(invocation -> {
            MedicineInventory inventory =
                    invocation.getArgument(0);

            MedicineDisposalRequest request =
                    invocation.getArgument(1);

            if (inventory.getId().equals(2L)) {
                throw new IllegalStateException(
                        "두 번째 재고 폐기 실패"
                );
            }

            inventory.disposeQuantity(request.quantity());

            // 첫 번째 재고 변경을 실제 DB에 반영한 뒤
            // 두 번째 처리에서 예외를 발생시킨다.
            entityManager.flush();

            return null;
        }).when(disposalOperation)
                .process(
                        any(MedicineInventory.class),
                        any(MedicineDisposalRequest.class)
                );
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("delete from medicine_inventorys");
        jdbcTemplate.update("delete from zones");
        jdbcTemplate.update("delete from storage_departments");
        jdbcTemplate.update("delete from storages");
        jdbcTemplate.update("delete from medicine_package_units");
        jdbcTemplate.update("delete from medicines");
        jdbcTemplate.update("delete from departments");
        jdbcTemplate.update("delete from organizations");
    }

    @Test
    @DisplayName("두 번째 재고 폐기에 실패하면 첫 번째 재고 변경도 롤백된다.")
    void disposeExpiredInventories_rollsBackAll() {
        ExpiredInventoryDisposalRequest request =
                new ExpiredInventoryDisposalRequest(
                        List.of(2L, 1L)
                );

        assertThrowsExactly(
                IllegalStateException.class,
                () -> medicineDisposalService
                        .disposeExpiredInventories(request)
        );

        Integer firstQuantity = jdbcTemplate.queryForObject(
                """
                select current_quantity
                from medicine_inventorys
                where inventory_id = 1
                """,
                Integer.class
        );

        Integer secondQuantity = jdbcTemplate.queryForObject(
                """
                select current_quantity
                from medicine_inventorys
                where inventory_id = 2
                """,
                Integer.class
        );

        String firstStatus = jdbcTemplate.queryForObject(
                """
                select management_status
                from medicine_inventorys
                where inventory_id = 1
                """,
                String.class
        );

        String secondStatus = jdbcTemplate.queryForObject(
                """
                select management_status
                from medicine_inventorys
                where inventory_id = 2
                """,
                String.class
        );

        assertAll(
                () -> assertEquals(3, firstQuantity),
                () -> assertEquals(5, secondQuantity),
                () -> assertEquals("NORMAL", firstStatus),
                () -> assertEquals("NORMAL", secondStatus)
        );
    }
    @Test
    @DisplayName("같은 만료 재고를 동시에 폐기하면 한 요청만 성공한다.")
    void disposeExpiredInventories_concurrentRequest() throws Exception {
        ExpiredInventoryDisposalRequest request =
                new ExpiredInventoryDisposalRequest(
                        List.of(1L)
                );

        ExecutorService executorService =
                Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Throwable> task = () -> {
            ready.countDown();
            start.await();

            try {
                medicineDisposalService
                        .disposeExpiredInventories(request);

                return null;
            } catch (Throwable throwable) {
                return throwable;
            }
        };

        Future<Throwable> firstFuture =
                executorService.submit(task);

        Future<Throwable> secondFuture =
                executorService.submit(task);

        try {
            assertTrue(
                    ready.await(5, TimeUnit.SECONDS),
                    "두 요청이 제한 시간 안에 준비되지 않았습니다."
            );

            start.countDown();

            Throwable firstResult =
                    firstFuture.get(10, TimeUnit.SECONDS);

            Throwable secondResult =
                    secondFuture.get(10, TimeUnit.SECONDS);

            long successCount =
                    Stream.of(firstResult, secondResult)
                            .filter(Objects::isNull)
                            .count();

            long rejectedCount =
                    Stream.of(firstResult, secondResult)
                            .filter(
                                    InvalidExpiredDisposalTargetException
                                            .class::isInstance
                            )
                            .count();

            Integer quantity = jdbcTemplate.queryForObject(
                    """
                    select current_quantity
                    from medicine_inventorys
                    where inventory_id = 1
                    """,
                    Integer.class
            );

            String status = jdbcTemplate.queryForObject(
                    """
                    select management_status
                    from medicine_inventorys
                    where inventory_id = 1
                    """,
                    String.class
            );

            assertAll(
                    () -> assertEquals(1, successCount),
                    () -> assertEquals(1, rejectedCount),
                    () -> assertEquals(
                            2,
                            successCount + rejectedCount
                    ),
                    () -> assertEquals(0, quantity),
                    () -> assertEquals("DISPOSAL", status)
            );
        } finally {
            executorService.shutdownNow();
        }
    }
}