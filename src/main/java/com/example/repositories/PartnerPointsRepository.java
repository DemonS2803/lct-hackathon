package com.example.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entities.PartnerPoint;

@Repository
public interface PartnerPointsRepository extends JpaRepository<PartnerPoint, Long> {
    
}
