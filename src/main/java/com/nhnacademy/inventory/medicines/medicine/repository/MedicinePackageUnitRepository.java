package com.nhnacademy.inventory.medicines.medicine.repository;


import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicinePackageUnitRepository extends JpaRepository<MedicinePackageUnit, Long> {


    @Query("""
            
            select pu
            from MedicinePackageUnit pu
            join fetch pu.medicine m
            where m.productName
                        like concat('%',:productName,'%')
            order by m.productName,pu.packUnit
            """)
    Page<MedicinePackageUnit> findAllWithMedicineByProductName(@Param("productName")String productName, Pageable pageable);



    @Query("""
           select pu
           from MedicinePackageUnit  pu
           join fetch pu.medicine m
           where pu.id = :packUnitId
    """)
    Optional<MedicinePackageUnit> findWithMedicineByPackUnitId(@Param("packUnitId")Long packUnitId);

    // Optional로 감싸는 이유
    // 해당 Id의 데이터가 없을 수도 있기 때문

}
