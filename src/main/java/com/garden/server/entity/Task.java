package com.garden.server.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plot_id")
    private Plot plot;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Хранится как timestamp в миллисекундах для совместимости с клиентом
    @Column(nullable = false)
    private Long date;

    @Column(name = "is_completed")
    private Boolean isCompleted;

    // JSON-строка с правилом повторения (RecurrenceRule)
    @Column(name = "recurrence_rule_json", columnDefinition = "TEXT")
    private String recurrenceRuleJson;

    @Column(name = "category_name", length = 50)
    private String categoryName;

    @Column(name = "planted_item_id")
    private Long plantedItemId;

    @Column(name = "planted_item_type", length = 20) // "BED" или "TREEBUSH"
    private String plantedItemType;
}