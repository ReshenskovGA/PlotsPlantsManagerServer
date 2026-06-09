package com.garden.server.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "plots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String address;

    @Column(name = "cadastral_number", length = 50)
    private String cadastralNumber;

    @Column(name = "length_m")
    private Double length;

    @Column(name = "width_m")
    private Double width;

    @ElementCollection
    @CollectionTable(name = "plot_photos", joinColumns = @JoinColumn(name = "plot_id"))
    @Column(name = "photo_uri")
    private List<String> photosUri;

    @Column(name = "plan_photo_uri", length = 255)
    private String planPhotoUri;
}