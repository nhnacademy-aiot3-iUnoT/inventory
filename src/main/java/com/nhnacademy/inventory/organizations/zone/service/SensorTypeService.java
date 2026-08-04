package com.nhnacademy.inventory.organizations.zone.service;

import com.nhnacademy.inventory.organizations.zone.domain.SensorType;
import com.nhnacademy.inventory.organizations.zone.dto.SensorTypeCreateRequest;
import com.nhnacademy.inventory.organizations.zone.dto.SensorTypeInfoResponse;
import com.nhnacademy.inventory.organizations.zone.exception.SensorTypeNameAlreadyExistsException;
import com.nhnacademy.inventory.organizations.zone.exception.SensorTypeNotFoundException;
import com.nhnacademy.inventory.organizations.zone.repository.SensorTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SensorTypeService {
    private final SensorTypeRepository sensorTypeRepository;

    @Transactional
    public SensorTypeInfoResponse createSensorType(SensorTypeCreateRequest request) {
        Optional<SensorType> sensorTypeOpt = sensorTypeRepository.findByName(request.name());

        if (sensorTypeOpt.isPresent()){
            throw new SensorTypeNameAlreadyExistsException();
        }

        SensorType sensorType = SensorType.builder()
                .name(request.name())
                .description(request.description())
                .build();

        SensorType saved = sensorTypeRepository.save(sensorType);

        return SensorTypeInfoResponse.from(saved);
    }

    public List<SensorTypeInfoResponse> getSensorTypes(){
        List<SensorType> sensorTypes = sensorTypeRepository.findAll();

        return sensorTypes.stream()
                .map(SensorTypeInfoResponse::from)
                .toList();
    }

    @Transactional
    public void deleteSensorType(Long sensorTypeId){
        SensorType sensorType = sensorTypeRepository.findById(sensorTypeId)
                .orElseThrow(SensorTypeNotFoundException::new);

        sensorTypeRepository.delete(sensorType);
    }
}
