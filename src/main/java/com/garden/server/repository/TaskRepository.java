package com.garden.server.repository;

import com.garden.server.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserId(Long userId);

    List<Task> findByPlotId(Long plotId);

    List<Task> findByPlantedItemIdAndPlantedItemType(Long plantedItemId, String plantedItemType);
}