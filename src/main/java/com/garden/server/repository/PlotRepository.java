package com.garden.server.repository;

import com.garden.server.entity.Plot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlotRepository extends JpaRepository<Plot, Long> {

    List<Plot> findByUserId(Long userId);
}