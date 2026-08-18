package com.nhnacademy.inventory.organizations.zone.service;

import com.nhnacademy.inventory.organizations.zone.domain.SensorType;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneThreshold;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdDetailResponse;
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
    public ThresholdDetailResponse saveThreshold(Long zoneId, ThresholdSaveRequest request){
        validateRange(request.minValue(), request.maxValue());

        Zone zone = zoneService.validateOwnerAndGetZone(zoneId);

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

        return ThresholdDetailResponse.from(threshold);
    }

    public List<ThresholdInfoResponse> getThresholds(Long zoneId){
        Zone zone = zoneService.validateMemberAndGetZone(zoneId);

        List<ZoneThreshold> thresholds = thresholdRepository.findAllByZone(zone);

        return thresholds.stream()
                .map(ThresholdInfoResponse::from)
                .toList();
    }

    public ThresholdDetailResponse getThreshold(Long zoneId, Long thresholdId){
        ZoneThreshold threshold = findByIdAndValidateMember(zoneId, thresholdId);

        return ThresholdDetailResponse.from(threshold);
    }

    public List<ThresholdSpecResponse> internalGetThresholds(Long zoneId){
        List<ZoneThreshold> zoneThreshold = thresholdRepository.findAllByZoneId(zoneId);

        return zoneThreshold.stream()
                .map(ThresholdSpecResponse::from)
                .toList();
    }

    @Transactional
    public void deleteThreshold(Long zoneId, Long thresholdId){
        ZoneThreshold threshold = findByIdAndValidateOwner(zoneId, thresholdId);

        thresholdRepository.delete(threshold);
    }

    private ZoneThreshold findByIdAndValidateOwner(Long zoneId, Long thresholdId){
        Zone zone = zoneService.validateOwnerAndGetZone(zoneId);

        return thresholdRepository.findByZoneThresholdIdAndZone(thresholdId, zone)
                .orElseThrow(ThresholdNotFoundException::new);
    }

    private ZoneThreshold findByIdAndValidateMember(Long zoneId, Long thresholdId){
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
