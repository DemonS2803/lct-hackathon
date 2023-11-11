package com.example.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entities.FromToDTO;

@Repository
public interface FromToDTORepository extends JpaRepository<FromToDTO, Long> {
    
}
