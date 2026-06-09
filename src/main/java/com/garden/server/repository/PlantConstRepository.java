package com.garden.server.repository;

import com.garden.server.entity.PlantConst;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlantConstRepository extends JpaRepository<PlantConst, Long> {

    List<PlantConst> findByNameContainingIgnoreCase(String name);

    // Поиск по категории (учитывая, что categories хранится в отдельной таблице через @ElementCollection,
    // Spring Data JPA корректно обработает этот запрос, но для больших объемов данных лучше использовать @Query с JOIN)
    List<PlantConst> findByCategoriesContaining(String category);
}