package com.nhnacademy.inventory.organizations.department.repository;

import com.nhnacademy.inventory.organizations.department.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
