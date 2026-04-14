package com.ahatravel.customer.repository;

import com.ahatravel.customer.entity.RefData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefDataRepository extends JpaRepository<RefData, Long> {

    List<RefData> findAllByCategoryAndActiveTrueOrderBySortOrderAsc(String category);

    Optional<RefData> findByCategoryAndCode(String category, String code);

    List<RefData> findAllByActiveTrue();
}
