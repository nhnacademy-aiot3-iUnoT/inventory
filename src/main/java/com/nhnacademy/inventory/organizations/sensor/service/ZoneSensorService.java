package com.nhnacademy.inventory.organizations.sensor.service;

import com.nhnacademy.inventory.organizations.sensor.domain.ZoneSensor;
import com.nhnacademy.inventory.organizations.sensor.dto.*;
import com.nhnacademy.inventory.organizations.sensor.exception.ZoneSensorAlreadyExistsException;
import com.nhnacademy.inventory.organizations.sensor.exception.ZoneSensorNotFoundException;
import com.nhnacademy.inventory.global.cache.CacheInvalidationEvent;
import com.nhnacademy.inventory.global.cache.CacheNames;
import com.nhnacademy.inventory.organizations.sensor.repository.ZoneSensorRepository;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.service.ZoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ZoneSensorService {

    private final ZoneSensorRepository zoneSensorRepository;
    private final ZoneService zoneService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ZoneSensorDetailResponse createZoneSensor(Long zoneId, ZoneSensorCreateRequest request){
        Zone zone = zoneService.validateOwnerAndGetZone(zoneId);

        zoneService.validateZoneStatus(zone);

        if(zoneSensorRepository.existsByDeviceEui(request.deviceEui())){
            throw new ZoneSensorAlreadyExistsException();
        }

        ZoneSensor saved = zoneSensorRepository.save(
                ZoneSensor.builder()
                        .zone(zone)
                        .deviceEui(request.deviceEui())
                        .name(request.name())
                        .description(request.description())
                        .build()
        );

        eventPublisher.publishEvent(
                CacheInvalidationEvent.of(CacheNames.DEVICE_ZONE, saved.getDeviceEui()));

        return ZoneSensorDetailResponse.from(saved);
    }

    public List<ZoneSensorInfoResponse> getZoneSensors(Long zoneId){
        Zone zone = zoneService.validateMemberAndGetZone(zoneId);

        List<ZoneSensor> zoneSensors = zoneSensorRepository.findAllByZone(zone);

        return zoneSensors.stream()
                .map(ZoneSensorInfoResponse::from)
                .toList();
    }

    public ZoneSensorDetailResponse getZoneSensor(Long zoneId, Long zoneSensorId){
        Zone zone = zoneService.validateOwnerAndGetZone(zoneId);

        ZoneSensor zoneSensor = zoneSensorRepository.findByIdAndZone(zoneSensorId, zone)
                .orElseThrow(ZoneSensorNotFoundException::new);

        return ZoneSensorDetailResponse.from(zoneSensor);
    }

    public DeviceLocationResponse getDeviceLocation(String deviceEui){
        ZoneSensor zoneSensor = zoneSensorRepository.findByDeviceEuiWithLocation(deviceEui)
                .orElseThrow(ZoneSensorNotFoundException::new);

        return DeviceLocationResponse.from(zoneSensor);
    }

    @Transactional
    public ZoneSensorDetailResponse updateZoneSensorInfo(Long zoneId, Long zoneSensorId, ZoneSensorUpdateRequest request){
        Zone zone = zoneService.validateOwnerAndGetZone(zoneId);

        zoneService.validateZoneStatus(zone);

        ZoneSensor zoneSensor = zoneSensorRepository.findByIdAndZone(zoneSensorId, zone)
                .orElseThrow(ZoneSensorNotFoundException::new);

        zoneSensor.updateInfo(request.name(), request.description());

        return ZoneSensorDetailResponse.from(zoneSensor);
    }

    @Transactional
    public void deleteZoneSensor(Long zoneId, Long zoneSensorId){
        Zone zone = zoneService.validateOwnerAndGetZone(zoneId);

        zoneService.validateZoneStatus(zone);

        ZoneSensor zoneSensor = zoneSensorRepository.findByIdAndZone(zoneSensorId, zone)
                .orElseThrow(ZoneSensorNotFoundException::new);

        zoneSensorRepository.delete(zoneSensor);

        eventPublisher.publishEvent(
                CacheInvalidationEvent.of(CacheNames.DEVICE_ZONE, zoneSensor.getDeviceEui()));
    }
}
