package com.nhnacademy.inventory.reports.environment.service;

import com.nhnacademy.inventory.reports.environment.domain.ReportEnvironmentDoorStat;
import com.nhnacademy.inventory.reports.environment.domain.ReportEnvironmentStat;
import com.nhnacademy.inventory.reports.environment.repository.ReportEnvironmentDoorStatRepository;
import com.nhnacademy.inventory.reports.environment.repository.ReportEnvironmentStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportEnvironmentService {

    private final ReportEnvironmentStatRepository statRepository;
    private final ReportEnvironmentDoorStatRepository doorStatRepository;

    // 환경 통계와 문열림 통계를 하나의 트랜잭션으로 저장
    @Transactional
    public void registerAll(List<ReportEnvironmentStat> stats, List<ReportEnvironmentDoorStat> doorStats) {
        statRepository.saveAll(stats);
        doorStatRepository.saveAll(doorStats);
    }

    @Transactional(readOnly = true)
    public List<ReportEnvironmentStat> getStats(Long reportId) {
        return statRepository.findAllByReportId(reportId);
    }

    @Transactional(readOnly = true)
    public List<ReportEnvironmentDoorStat> getDoorStats(Long reportId) {
        return doorStatRepository.findAllByReportId(reportId);
    }
}
