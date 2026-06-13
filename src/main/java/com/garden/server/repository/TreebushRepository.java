package com.garden.server.repository;

import com.garden.server.entity.Treebush;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TreebushRepository extends JpaRepository<Treebush, Long> {
    long countByUserId(Long userId);

    List<Treebush> findByPlotId(Long plotId);

    List<Treebush> findByUserId(Long userId);
}