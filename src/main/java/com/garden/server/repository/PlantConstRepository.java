package com.garden.server.repository;

import com.garden.server.entity.PlantConst;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlantConstRepository extends JpaRepository<PlantConst, Long> {

    List<PlantConst> findByNameContainingIgnoreCase(String name);
    List<PlantConst> findByCategoriesContaining(String category);
}