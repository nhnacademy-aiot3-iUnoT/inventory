package com.nhnacademy.inventory.medicines.enviroment.service;

import com.nhnacademy.inventory.medicines.enviroment.domain.EnvironmentCheckStatus;
import com.nhnacademy.inventory.medicines.enviroment.dto.EnvironmentCheckResponse;
import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentTypeResponse;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdInfoResponse;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdSpecResponse;
import com.nhnacademy.inventory.organizations.zone.service.ThresholdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnvironmentCheckService {

    private final EnvironmentTypeSearchService searchService;
    private final ThresholdService thresholdService;


    public EnvironmentCheckResponse check(Long packUnitId, Long zoneId){


        //의약품 임계값 조회
        List<MedicineEnvironmentTypeResponse> types = searchService.getTypes(packUnitId);

        // 구역 환경 임계값 조회
        List<ThresholdInfoResponse> thresholdResponses = thresholdService.getThresholds(zoneId);






        return  null;



    }

    private EnvironmentCheckResponse checkEnvironment(String sensorName, BigDecimal medicineMin, BigDecimal medicineMax, BigDecimal zoneMin, BigDecimal zoneMax){


        if(medicineMin == null && medicineMax == null){

            return new EnvironmentCheckResponse(EnvironmentCheckStatus.COMPATIBLE,
                    List.of()
                    );

        }

        else if(zoneMin == null && zoneMax == null){

            return new EnvironmentCheckResponse(EnvironmentCheckStatus.INCOMPATIBLE,
                    List.of(sensorName + "확인 불가" + "(구역 임계값 없음, 의약품: " + medicineMin + "~" + medicineMax)
                    );

        }

//
//        if(medicineMin != null && medicineMax != null){
//
//            if(!(medicineMin.compareTo(zoneMin) <= 0 && zoneMax.compareTo(medicineMax) <= 0)){
//
//                return new EnvironmentCheckResponse(EnvironmentCheck)
//
//
//            }
//
//
//        }


        return null;






    }








}
