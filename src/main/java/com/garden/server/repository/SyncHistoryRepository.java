package com.garden.server.repository;

import com.garden.server.entity.SyncHistory;
import com.garden.server.enums.EntityType;
import com.garden.server.enums.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface SyncHistoryRepository extends JpaRepository<SyncHistory, Long> {

    List<SyncHistory> findBySyncStatusOrderByCreatedAtAsc(SyncStatus status);

    List<SyncHistory> findByEntityTypeAndSyncStatus(EntityType entityType, SyncStatus status);

    @Modifying
    @Transactional
    @Query("DELETE FROM SyncHistory s WHERE s.syncStatus = :status")
    void deleteBySyncStatus(SyncStatus status);
}