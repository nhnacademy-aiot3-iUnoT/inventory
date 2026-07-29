package com.nhnacademy.inventory.medicines.medicine.dto;

import java.util.List;

public record MedicineResponse(

        String itemCode,
        String productName,
        String companyName,
        String storageMethod,
        String validityPeriod,
        List<String> packageUnits,
        String narcoticKindCode


) {

    //품목기준코드  | String         |   필수  | 의약품을 식별하는 고유 업무 코드     |
    //| 제품명     | String         |   필수  | 의약품의 허가된 제품명           |
    //| 업체명     | String         |   필수  | 의약품 제조 또는 판매 업체명       |
    //| 저장방법    | String         |   선택  | 의약품의 보관 방법에 대한 원문 정보   |
    //| 유효기간    | String         |   선택  | 제조일을 기준으로 허가된 사용 가능 기간 |
    //| 포장단위    | String         |   선택  | 의약품의 포장 수량 및 단위 정보     |
    //| 성분 및 함량 | String         |   선택  | 의약품의 성분명과 함량에 대한 원문 정보 |
    //| 마약류 구분
}
