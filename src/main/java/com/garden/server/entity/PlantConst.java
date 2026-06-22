package com.garden.server.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "plants_const")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantConst {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "plant_const_categories", joinColumns = @JoinColumn(name = "plant_const_id"))
    @Column(name = "category")
    private List<String> categories;

    @ElementCollection
    @CollectionTable(name = "plant_const_photos", joinColumns = @JoinColumn(name = "plant_const_id"))
    @Column(name = "photo_uri")
    private List<String> photosUri;
}