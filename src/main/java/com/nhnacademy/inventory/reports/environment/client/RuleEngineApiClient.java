package com.nhnacademy.inventory.reports.environment.client;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.reports.environment.dto.SensorLatestResponse;
import com.nhnacademy.inventory.reports.environment.dto.StorageDailySummaryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class RuleEngineApiClient {

    private static final String DAILY_SUMMARIES_PATH = "/api/rule-engine/internal/storages/{storageId}/daily-summaries";
    private static final String LATEST_SENSORS_PATH = "/api/rule-engine/internal/storages/{storageId}/sensor-data/latest";

    private final RestClient restClient;

    public RuleEngineApiClient(@Qualifier("ruleEngineRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

/**
     * 저장소의 구역별 센서 최신값을 조회
     * 환경 현황의 보조 정보이므로 조회에 실패해도 예외를 던지지 않고 빈 목록을 반환함
     */
    public List<SensorLatestResponse> findLatestSensors(Long organizationId, Long storageId) {
        try {
            ApiResponse<List<SensorLatestResponse>> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(LATEST_SENSORS_PATH)
                            .queryParam("organizationId", organizationId)
                            .build(storageId))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null || !response.success() || response.data() == null) {
                log.warn("룰엔진 센서 최신값 응답이 비어 있습니다. storageId={}", storageId);
                return List.of();
            }

            return response.data();
        } catch (RestClientException e) {
            log.warn("룰엔진 센서 최신값 조회에 실패했습니다. storageId={}", storageId, e);
            return List.of();
        }
    }

    /**
     * 저장소의 일별 환경 요약을 기간으로 조회
     * 환경 데이터는 리포트의 부수적인 정보이므로, 조회에 실패해도 예외를 던지지 않고 빈 목록을 반환함
     */
    public List<StorageDailySummaryResponse> findDailySummaries(Long storageId, LocalDate from, LocalDate to) {
        try {
            ApiResponse<List<StorageDailySummaryResponse>> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(DAILY_SUMMARIES_PATH)
                            .queryParam("from", from)
                            .queryParam("to", to)
                            .build(storageId))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null || !response.success() || response.data() == null) {
                log.warn("룰엔진 환경 요약 응답이 비어 있습니다. storageId={}, from={}, to={}", storageId, from, to);
                return List.of();
            }

            return response.data();
        } catch (RestClientException e) {
            log.warn("룰엔진 환경 요약 조회에 실패했습니다. storageId={}, from={}, to={}", storageId, from, to, e);
            return List.of();
        }
    }
}
