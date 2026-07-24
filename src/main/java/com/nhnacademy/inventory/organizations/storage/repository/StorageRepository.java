package com.nhnacademy.inventory.organizations.storage.repository;

import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageRepository extends JpaRepository<Storage, Long> {
}
