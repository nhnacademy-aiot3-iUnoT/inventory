package com.nhnacademy.inventory.enviroments.review.repository;

import com.nhnacademy.inventory.enviroments.review.domain.EnvironmentReview;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnvironmentReviewRepository extends JpaRepository<EnvironmentReview, Long>, EnvironmentReviewRepositoryCustom {

    @Query("SELECT r FROM EnvironmentReview r " +
            "JOIN FETCH r.medicineInventory mi " +
            "JOIN FETCH mi.zone z " +
            "JOIN FETCH z.storage s " +
            "JOIN FETCH s.organization o " +
            "JOIN FETCH mi.medicinePackageUnit mpu " +
            "JOIN FETCH mpu.medicine m " +
            "WHERE mi = :inventory")
    List<EnvironmentReview> findAllByMedicineInventoryWithFetch(@Param("inventory") MedicineInventory inventory);

}
