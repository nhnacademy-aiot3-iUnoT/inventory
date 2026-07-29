package com.nhnacademy.inventory.medicines.medicine.service;

import com.nhnacademy.inventory.medicines.medicine.client.MedicineApiClient;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicineResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineApiService {

    private final MedicineApiClient medicineApiClient;
    private final MedicineSaveService medicineSaveService;
    private final ObjectMapper objectMapper;
    private static final int PAGE_SIZE = 500;


    public void savedAllMedicines(){


        String json = medicineApiClient.getJson(1,PAGE_SIZE);
        //log.info("json: {}",json);

        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode jsonBody = jsonNode.path("body");
        JsonNode items = jsonBody.path("items");

        List<MedicineResponse> firstPageResponse = convertToMedicineResponse(items);

        medicineSaveService.saveMedicines(firstPageResponse);



        int totalCount = jsonBody.path("totalCount").asInt();
        //int totalCount = 100;
        log.info("totalCount: {}",totalCount);

        // 전체 페이지 수 구하기
        int totalPages = (totalCount + PAGE_SIZE - 1) / PAGE_SIZE;

        log.info("의약품 저장 완료. page= {}/{} size= {}",1,totalPages,firstPageResponse.size());


        for(int pageNo = 2; pageNo <= totalPages; pageNo++){

            String forJson = medicineApiClient.getJson(pageNo,PAGE_SIZE);
            JsonNode pageJson= objectMapper.readTree(forJson);
            JsonNode pageItems = pageJson.path("body").path("items");
            List<MedicineResponse> pageResponse = convertToMedicineResponse(pageItems);
            medicineSaveService.saveMedicines(pageResponse);

            log.info("의약품 저장 완료. page= {}/{} size= {}",pageNo,totalPages,pageResponse.size());

        }






    }


    private List<MedicineResponse> convertToMedicineResponse(JsonNode items){

        List<MedicineResponse> result = new ArrayList<>();


        if(!items.isArray() || items.isEmpty()){
            return result;
        }


        for(JsonNode item : items){

            String itemCode = item.path("ITEM_SEQ").asString(null);
            String productName = item.path("ITEM_NAME").asString(null);
            String companyName = item.path("ENTP_NAME").asString(null);
            String validityPeriod = item.path("VALID_TERM").asString(null);
            String packUnit = item.path("PACK_UNIT").asString(null);
            String narcoticKindCode = item.path("NARCOTIC_KIND_CODE").asString(null);
            String storageMethod = item.path("STORAGE_METHOD").asString(null);


            List<String> units  = parsingPackageUnit(packUnit);

            MedicineResponse response = new MedicineResponse(itemCode,productName,companyName,storageMethod,validityPeriod,units,narcoticKindCode);
            result.add(response);

        }


        return result;

    }


    private List<String> parsingPackageUnit(String packageUnit){

        //500mL/병, 1000mL/병, 500mL/백, 1000mL/백

        List<String> units = new ArrayList<>();

        if(packageUnit == null || packageUnit.isBlank() || packageUnit.equals(".")){
            return List.of("포장단위 정보 없음");
        }

        String[] parts = packageUnit.trim().split("[,\\r\\n]+");

        for (String part : parts){

            String unit = part.trim();


            if(unit.isBlank()){
                continue;
            }

            if(unit.contains("수출용")){
                continue;
            }
            units.add(unit);

        }

        if(packageUnit.isEmpty()){
            return List.of("포장단위 정보 없음");
        }


        return units;

    }




}
