package com.nhnacademy.inventory.reports.environment.repository;

import com.nhnacademy.inventory.reports.environment.domain.ReportEnvironmentStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportEnvironmentStatRepository extends JpaRepository<ReportEnvironmentStat, Long> {
}
