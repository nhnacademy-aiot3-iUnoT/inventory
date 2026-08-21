package com.nhnacademy.inventory.reports.report.repository;

import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.domain.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query("""
        SELECT r
        FROM Report r
        LEFT JOIN FETCH r.reportItems
        WHERE r.id = :reportId
        """)
    Optional<Report> findByIdWithItems(@Param("reportId") Long reportId);

    @Query("""
        SELECT r
        FROM Report r
        LEFT JOIN FETCH r.reportItems
        WHERE r.id = :reportId AND r.organizationId = :organizationId
        """)
    Optional<Report> findByIdAndOrganizationId(
            @Param("reportId") Long reportId, @Param("organizationId") Long organizationId);

    @Query("""
        SELECT r
        FROM Report r
        LEFT JOIN FETCH r.reportItems
        WHERE r.id = :reportId AND r.storageId = :storageId
        """)
    Optional<Report> findByIdAndStorageId(
            @Param("reportId") Long reportId, @Param("storageId") Long storageId);

    @Query("""
        SELECT r
        FROM Report r
        LEFT JOIN FETCH r.reportItems
        WHERE r.storageId = :storageId AND r.reportType = :reportType AND r.periodStart = :periodStart
        """)
    Optional<Report> findByStorageIdAndReportTypeAndPeriodStartWithItems(
            @Param("storageId") Long storageId,
            @Param("reportType") ReportType reportType,
            @Param("periodStart") LocalDate periodStart);

    Optional<Report> findByStorageIdAndReportTypeAndPeriodStart(
            Long storageId, ReportType reportType, LocalDate periodStart);
}
