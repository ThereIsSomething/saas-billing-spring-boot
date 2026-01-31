package com.project.saasbilling.repository;

import com.project.saasbilling.model.Plan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for Plan documents.
 */
@Repository
public interface PlanRepository extends MongoRepository<Plan, String> {

    List<Plan> findByActiveTrue();

    List<Plan> findByActiveTrueOrderBySortOrderAsc();

    Optional<Plan> findByName(String name);

    boolean existsByName(String name);
}
