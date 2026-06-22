package com.garden.server.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "plants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "plant_categories", joinColumns = @JoinColumn(name = "plant_id"))
    @Column(name = "category")
    private List<String> categories;

    @ElementCollection
    @CollectionTable(name = "plant_photos", joinColumns = @JoinColumn(name = "plant_id"))
    @Column(name = "photo_uri")
    private List<String> photosUri;
}