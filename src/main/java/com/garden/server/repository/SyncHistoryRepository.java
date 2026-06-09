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

    // Получение всех неподтвержденных записей, отсортированных по времени создания (FIFO)
    List<SyncHistory> findBySyncStatusOrderByCreatedAtAsc(SyncStatus status);

    // Получение записей конкретного типа (например, только PLOT) со статусом PENDING
    List<SyncHistory> findByEntityTypeAndSyncStatus(EntityType entityType, SyncStatus status);

    // Массовое удаление успешно синхронизированных записей для очистки таблицы
    @Modifying
    @Transactional
    @Query("DELETE FROM SyncHistory s WHERE s.syncStatus = :status")
    void deleteBySyncStatus(SyncStatus status);
}