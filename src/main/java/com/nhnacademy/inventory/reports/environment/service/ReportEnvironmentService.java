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

    /**
     * 센서 통계와 문 통계를 한 트랜잭션으로 저장한다.
     * 둘이 따로 커밋되면 센서만 저장된 반쪽 상태가 남을 수 있다.
     */
    @Transactional
    public void registerAll(List<ReportEnvironmentStat> stats, List<ReportEnvironmentDoorStat> doorStats) {
        statRepository.saveAll(stats);
        doorStatRepository.saveAll(doorStats);
    }
}
