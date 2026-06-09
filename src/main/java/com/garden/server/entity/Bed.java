package com.garden.server.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "beds")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plot_id", nullable = false)
    private Plot plot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Может быть отрицательным (справочник) или положительным (личное растение)
    @Column(name = "plant_id")
    private Long plantId;

    @Column(name = "length_m")
    private Double length;

    @Column(name = "width_m")
    private Double width;

    @Column(name = "offset_x_m")
    private Double offsetX;

    @Column(name = "offset_y_m")
    private Double offsetY;

    @Column(name = "marker_color")
    private Integer markerColor;
}
