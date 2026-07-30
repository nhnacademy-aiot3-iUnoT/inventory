package com.nhnacademy.inventory.organizations.zone.service;

import com.nhnacademy.inventory.organizations.zone.domain.SensorType;
import com.nhnacademy.inventory.organizations.zone.dto.SensorTypeCreateRequest;
import com.nhnacademy.inventory.organizations.zone.dto.SensorTypeInfoResponse;
import com.nhnacademy.inventory.organizations.zone.exception.SensorTypeNameAlreadyExistsException;
import com.nhnacademy.inventory.organizations.zone.exception.SensorTypeNotFoundException;
import com.nhnacademy.inventory.organizations.zone.repository.SensorTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.Mockito.never;
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

    @Nested
    @DisplayName("센서타입 생성 테스트")
    class createSensorType{

        @Test
        @DisplayName("성공 테스트")
        void success() {
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
        @DisplayName("실패 이름 중복")
        void fail_DuplicateName() {
            SensorTypeCreateRequest request = new SensorTypeCreateRequest("테스트 센서1", "테스트 설명2");

            given(sensorTypeRepository.findByName(sensorType.getName()))
                    .willReturn(Optional.of(sensorType));

            assertThrowsExactly(SensorTypeNameAlreadyExistsException.class, () ->
                    sensorTypeService.createSensorType(request)
            );

            verify(sensorTypeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("센서탕비 조회 테스트")
    class getSensorTypes{

        @Test
        @DisplayName("성공 테스트")
        void success() {
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
        @DisplayName("성공 테스트 (빈 리스트)")
        void success_empty() {
            given(sensorTypeRepository.findAll()).willReturn(List.of());

            List<SensorTypeInfoResponse> responses = sensorTypeService.getSensorTypes();

            assertEquals(0, responses.size());

            verify(sensorTypeRepository).findAll();
        }
    }

    @Nested
    @DisplayName("센서타입 삭제 테스트")
    class deleteSensorType{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            given(sensorTypeRepository.findById(sensorType.getSensorTypeId()))
                    .willReturn(Optional.of(sensorType));

            assertDoesNotThrow(() ->
                    sensorTypeService.deleteSensorType(sensorType.getSensorTypeId())
            );

            verify(sensorTypeRepository).findById(anyLong());
            verify(sensorTypeRepository).delete(any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 센서타입")
        void fail_NotFoundSensorType() {
            given(sensorTypeRepository.findById(anyLong()))
                    .willReturn(Optional.empty());

            assertThrowsExactly(SensorTypeNotFoundException.class, () ->
                    sensorTypeService.deleteSensorType(333L)
            );

            verify(sensorTypeRepository, never()).delete(any());
        }
    }

    private void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("sensorTypeId");
        field.setAccessible(true);
        field.set(entity, id);
    }
}