package com.nhnacademy.inventory.reports.environment.repository;

import com.nhnacademy.inventory.reports.environment.domain.ReportEnvironmentDoorStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportEnvironmentDoorStatRepository extends JpaRepository<ReportEnvironmentDoorStat, Long> {

    List<ReportEnvironmentDoorStat> findAllByReportId(Long reportId);
}
