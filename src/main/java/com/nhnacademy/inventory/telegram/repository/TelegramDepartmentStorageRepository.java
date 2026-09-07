package com.nhnacademy.inventory.telegram.repository;

import com.nhnacademy.inventory.organizations.department.domain.StorageDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 단톡방에 연결된 부서가 접근할 수 있는 저장소를 조회한다.
 * 기존 StorageDepartmentRepository를 수정하지 않으려고 텔레그램 전용으로 따로 둔다.
 */
public interface TelegramDepartmentStorageRepository extends JpaRepository<StorageDepartment, Long> {

    @Query("""
        select distinct sd.storage.id
        from StorageDepartment sd
        where sd.department.id = :departmentId
          and sd.storage.status <> 'CLOSED'
    """)
    List<Long> findAccessibleStorageIdsByDepartmentId(Long departmentId);
}
