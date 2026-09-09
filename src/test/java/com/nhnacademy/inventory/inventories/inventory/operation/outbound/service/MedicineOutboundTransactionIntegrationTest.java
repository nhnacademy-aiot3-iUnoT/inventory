package com.nhnacademy.inventory.inventories.inventory.operation.outbound.service;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.inventories.inventory.exception.InsufficientStockException;
import com.nhnacademy.inventory.inventories.inventory.operation.InventoryOperationAccessValidator;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.domain.DisposalReason;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto.MedicineDisposalRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.service.MedicineDisposalService;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.domain.OutboundReason;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.dto.MedicineOutboundRequest;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Sql("/sql/inventory-test-data.sql")
class MedicineOutboundTransactionIntegrationTest {

    @Autowired
    private MedicineOutboundService medicineOutboundService;

    @Autowired
    private MedicineDisposalService medicineDisposalService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MedicineInventoryRepository medicineInventoryRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private InventoryOperationAccessValidator accessValidator;

    @MockitoBean
    private ApplicationEventPublisher eventPublisher;

    @AfterEach
    void tearDown() {
        UserContext.clear();

        jdbcTemplate.update("delete from stock_transactions");
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
    @DisplayName("동시 출고")
    void sameInventoryConcurrentOutbound_createsOneTransaction() throws Exception {
        MedicineOutboundRequest request = new MedicineOutboundRequest(
                1L,
                70,
                1L,
                OutboundReason.DISPENSING,
                null
        );

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Throwable> task = () -> {
            UserContext.setUserUuid(UUID.randomUUID());
            ready.countDown();
            start.await();

            try {
                medicineOutboundService.outbound(1L, request);
                return null;
            } catch (Throwable throwable) {
                return throwable;
            } finally {
                UserContext.clear();
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            Future<Throwable> firstFuture = executorService.submit(task);
            Future<Throwable> secondFuture = executorService.submit(task);

            assertTrue(ready.await(5, TimeUnit.SECONDS));

            start.countDown();

            Throwable firstResult = firstFuture.get(10, TimeUnit.SECONDS);
            Throwable secondResult = secondFuture.get(10, TimeUnit.SECONDS);

            long successCount = Stream.of(firstResult, secondResult)
                    .filter(Objects::isNull)
                    .count();

            long rejectedCount = Stream.of(firstResult, secondResult)
                    .filter(InsufficientStockException.class::isInstance)
                    .count();

            Integer quantity = jdbcTemplate.queryForObject(
                    """
                    select current_quantity
                    from medicine_inventorys
                    where inventory_id = 1
                    """,
                    Integer.class
            );

            Integer transactionCount = jdbcTemplate.queryForObject(
                    """
                    select count(*)
                    from stock_transactions
                    where transaction_type = 'OUTBOUND'
                    """,
                    Integer.class
            );

            assertAll(
                    () -> assertEquals(1, successCount),
                    () -> assertEquals(1, rejectedCount),
                    () -> assertEquals(30, quantity),
                    () -> assertEquals(1, transactionCount)
            );
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    @DisplayName("출고·폐기 동시 요청")
    void outboundAndDisposalConcurrentRequest_createsOneTransaction() throws Exception {
        MedicineOutboundRequest outboundRequest = new MedicineOutboundRequest(
                1L,
                60,
                1L,
                OutboundReason.DISPENSING,
                null
        );

        MedicineDisposalRequest disposalRequest = new MedicineDisposalRequest(
                60,
                DisposalReason.DETERIORATED,
                null
        );

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Throwable> outboundTask = () -> {
            UserContext.setUserUuid(UUID.randomUUID());
            ready.countDown();
            start.await();

            try {
                medicineOutboundService.outbound(1L, outboundRequest);
                return null;
            } catch (Throwable throwable) {
                return throwable;
            } finally {
                UserContext.clear();
            }
        };

        Callable<Throwable> disposalTask = () -> {
            UserContext.setUserUuid(UUID.randomUUID());
            ready.countDown();
            start.await();

            try {
                medicineDisposalService.dispose(1L, disposalRequest);
                return null;
            } catch (Throwable throwable) {
                return throwable;
            } finally {
                UserContext.clear();
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            Future<Throwable> outboundFuture = executorService.submit(outboundTask);
            Future<Throwable> disposalFuture = executorService.submit(disposalTask);

            assertTrue(ready.await(5, TimeUnit.SECONDS));

            start.countDown();

            Throwable outboundResult = outboundFuture.get(10, TimeUnit.SECONDS);
            Throwable disposalResult = disposalFuture.get(10, TimeUnit.SECONDS);

            long successCount = Stream.of(outboundResult, disposalResult)
                    .filter(Objects::isNull)
                    .count();

            long rejectedCount = Stream.of(outboundResult, disposalResult)
                    .filter(InsufficientStockException.class::isInstance)
                    .count();

            Integer quantity = jdbcTemplate.queryForObject(
                    """
                    select current_quantity
                    from medicine_inventorys
                    where inventory_id = 1
                    """,
                    Integer.class
            );

            Integer transactionCount = jdbcTemplate.queryForObject(
                    """
                    select count(*)
                    from stock_transactions
                    where zone_id = 1
                      and transaction_type in ('OUTBOUND', 'DISPOSAL')
                    """,
                    Integer.class
            );

            assertAll(
                    () -> assertEquals(1, successCount),
                    () -> assertEquals(1, rejectedCount),
                    () -> assertEquals(40, quantity),
                    () -> assertEquals(1, transactionCount)
            );
        } finally {
            executorService.shutdownNow();
        }
    }
    @Test
    @DisplayName("락 시간 초과")
    void outboundWithLockTimeout() throws Exception {
        MedicineOutboundRequest request = new MedicineOutboundRequest(
                1L,
                10,
                1L,
                OutboundReason.DISPENSING,
                null
        );

        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        Future<?> lockHolder = executorService.submit(() ->
                transactionTemplate.executeWithoutResult(status -> {
                    medicineInventoryRepository.findByIdForUpdate(1L);
                    locked.countDown();

                    try {
                        release.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                })
        );

        try {
            assertTrue(locked.await(5, TimeUnit.SECONDS));

            UserContext.setUserUuid(UUID.randomUUID());

            Throwable result;
            try {
                medicineOutboundService.outbound(1L, request);
                result = null;
            } catch (Throwable throwable) {
                result = throwable;
            } finally {
                UserContext.clear();
            }

            Integer quantity = jdbcTemplate.queryForObject(
                    """
                    select current_quantity
                    from medicine_inventorys
                    where inventory_id = 1
                    """,
                    Integer.class
            );

            Integer transactionCount = jdbcTemplate.queryForObject(
                    """
                    select count(*)
                    from stock_transactions
                    where transaction_type = 'OUTBOUND'
                    """,
                    Integer.class
            );

            assertTrue(result instanceof PessimisticLockingFailureException);

            assertAll(
                    () -> assertEquals(100, quantity),
                    () -> assertEquals(0, transactionCount)
            );
        } finally {
            release.countDown();
            lockHolder.get(5, TimeUnit.SECONDS);
            executorService.shutdownNow();
        }
    }
}