package com.garden.server.repository;

import com.garden.server.entity.Bed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BedRepository extends JpaRepository<Bed, Long> {

    List<Bed> findByPlotId(Long plotId);

    List<Bed> findByUserId(Long userId);
}