package com.nhnacademy.inventory.telegram.service;

import com.nhnacademy.inventory.telegram.repository.TelegramDepartmentStorageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 단톡방에 연결된 부서가 접근할 수 있는 저장소를 정한다.
 * 웹 챗봇의 ChatbotStorageAccessService와 같은 역할이며, 기준이 조직원이 아니라 부서라는 점만 다르다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TelegramStorageAccessService {

    private final TelegramDepartmentStorageRepository departmentStorageRepository;

    public List<Long> getAccessibleStorageIds(Long departmentId) {
        return departmentStorageRepository.findAccessibleStorageIdsByDepartmentId(departmentId);
    }
}
