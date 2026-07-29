package com.nhnacademy.inventory.organizations.zone.service;

import com.nhnacademy.inventory.organizations.zone.domain.SensorType;
import com.nhnacademy.inventory.organizations.zone.dto.SensorTypeCreateRequest;
import com.nhnacademy.inventory.organizations.zone.dto.SensorTypeInfoResponse;
import com.nhnacademy.inventory.organizations.zone.repository.SensorTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SensorTypeServiceTest {
    @Mock
    private SensorTypeRepository sensorTypeRepository;

    @InjectMocks
    private SensorTypeService sensorTypeService;

    private SensorType sensorType;

    @BeforeEach
    void setUp() throws Exception {
        sensorType = SensorType.builder()
                .name("테스트 센서1")
                .description("테스트 설명1")
                .build();

        setId(sensorType, 1L);
    }

    @Test
    @DisplayName("센서타입 생성 성공 테스트")
    void createSensorType() {
        SensorTypeCreateRequest request = new SensorTypeCreateRequest("테스트 센서2", "테스트 설명2");

        given(sensorTypeRepository.findByName(request.name()))
                .willReturn(Optional.empty());
        given(sensorTypeRepository.save(any(SensorType.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        SensorTypeInfoResponse response = sensorTypeService.createSensorType(request);

        assertAll(
                () -> assertEquals("테스트 센서2", response.name()),
                () -> assertEquals("테스트 설명2", response.description())
        );

        verify(sensorTypeRepository).findByName(anyString());
        verify(sensorTypeRepository).save(any());
    }

    @Test
    @DisplayName("센서타입 조회 성공 테스트")
    void getSensorTypes() {
        given(sensorTypeRepository.findAll()).willReturn(List.of(sensorType));

        List<SensorTypeInfoResponse> responses = sensorTypeService.getSensorTypes();

        assertAll(
                () -> assertEquals(1, responses.size()),
                () -> assertEquals(1L, responses.getFirst().sensorTypeId()),
                () -> assertEquals("테스트 센서1", responses.getFirst().name()),
                () -> assertEquals("테스트 설명1", responses.getFirst().description())
        );

        verify(sensorTypeRepository).findAll();
    }

    @Test
    @DisplayName("센서타입 삭제 성공 테스트")
    void deleteSensorType() {
        given(sensorTypeRepository.findById(sensorType.getSensorTypeId()))
                .willReturn(Optional.of(sensorType));

        assertDoesNotThrow(() ->
            sensorTypeService.deleteSensorType(sensorType.getSensorTypeId())
        );

        verify(sensorTypeRepository).findById(anyLong());
        verify(sensorTypeRepository).delete(any());
    }

    private void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("sensorTypeId");
        field.setAccessible(true);
        field.set(entity, id);
    }
}