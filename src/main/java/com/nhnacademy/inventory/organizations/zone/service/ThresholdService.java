package com.nhnacademy.inventory.organizations.zone.service;

import com.nhnacademy.inventory.organizations.zone.domain.SensorType;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneThreshold;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdSaveRequest;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdInfoResponse;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdSpecResponse;
import com.nhnacademy.inventory.organizations.zone.exception.SensorTypeNotFoundException;
import com.nhnacademy.inventory.organizations.zone.exception.ThresholdInvalidRangeException;
import com.nhnacademy.inventory.organizations.zone.exception.ThresholdNotFoundException;
import com.nhnacademy.inventory.organizations.zone.repository.SensorTypeRepository;
import com.nhnacademy.inventory.organizations.zone.repository.ThresholdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThresholdService {
    private final ThresholdRepository thresholdRepository;
    private final SensorTypeRepository sensorTypeRepository;
    private final ZoneService zoneService;

    private static final BigDecimal MIN_RANGE_GAP = BigDecimal.valueOf(5);

    @Transactional
    public ThresholdInfoResponse saveThreshold(Long zoneId, ThresholdSaveRequest request){
        validateRange(request.minValue(), request.maxValue());

        Zone zone = zoneService.validateMemberAndGetZone(zoneId);

        SensorType sensorType = sensorTypeRepository.findById(request.sensorTypeId())
                .orElseThrow(SensorTypeNotFoundException::new);

        ZoneThreshold threshold = thresholdRepository.findByZoneAndSensorType(zone, sensorType)
                .map(existing -> {
                    existing.updateValues(request.minValue(), request.maxValue(), request.alertDuration());
                    return existing;
                })
                .orElseGet(() -> thresholdRepository.save(
                        ZoneThreshold.builder()
                                .zone(zone)
                                .sensorType(sensorType)
                                .minValue(request.minValue())
                                .maxValue(request.maxValue())
                                .alertDuration(request.alertDuration())
                                .build()
                ));

        return ThresholdInfoResponse.from(threshold);
    }

    public List<ThresholdInfoResponse> getThresholds(Long zoneId){
        Zone zone = zoneService.validateMemberAndGetZone(zoneId);

        List<ZoneThreshold> thresholds = thresholdRepository.findAllByZone(zone);

        return thresholds.stream()
                .map(ThresholdInfoResponse::from)
                .toList();
    }

    public List<ThresholdSpecResponse> internalGetThresholds(Long zoneId){
        List<ZoneThreshold> zoneThreshold = thresholdRepository.findAllByZoneId(zoneId);

        return zoneThreshold.stream()
                .map(ThresholdSpecResponse::from)
                .toList();
    }

    @Transactional
    public void deleteThreshold(Long zoneId, Long thresholdId){
        ZoneThreshold threshold = findByIdAndValidate(zoneId, thresholdId);

        thresholdRepository.delete(threshold);
    }

    private ZoneThreshold findByIdAndValidate(Long zoneId, Long thresholdId){
        Zone zone = zoneService.validateMemberAndGetZone(zoneId);

        return thresholdRepository.findByZoneThresholdIdAndZone(thresholdId, zone)
                .orElseThrow(ThresholdNotFoundException::new);
    }

    private void validateRange(BigDecimal min, BigDecimal max){
        if (min == null || max == null){
            return;
        }

        BigDecimal gap = max.subtract(min);

        if(gap.compareTo(MIN_RANGE_GAP) < 0){
            throw new ThresholdInvalidRangeException();
        }
    }
}
