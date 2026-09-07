package com.nhnacademy.inventory.medicines.enviroment.dto;

import com.nhnacademy.inventory.medicines.enviroment.domain.EnvironmentCheckStatus;

import java.util.List;

public record EnvironmentCheckResponse(

        EnvironmentCheckStatus environmentCheckStatus,
        List<String> reasons

) {



}
