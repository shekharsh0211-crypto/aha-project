package com.ahatravel.customer.repository;

import com.ahatravel.customer.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Page<Vehicle> findAllByActiveTrue(Pageable pageable);

    boolean existsByPlateNumber(String plateNumber);
}
