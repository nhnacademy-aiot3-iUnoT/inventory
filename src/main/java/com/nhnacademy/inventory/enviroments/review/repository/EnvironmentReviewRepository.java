package com.nhnacademy.inventory.enviroments.review.repository;

import com.nhnacademy.inventory.enviroments.review.domain.EnvironmentReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvironmentReviewRepository extends JpaRepository<EnvironmentReview, Long> {
}
