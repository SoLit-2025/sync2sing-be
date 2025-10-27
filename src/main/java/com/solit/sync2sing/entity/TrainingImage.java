package com.solit.sync2sing.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

public class TrainingImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "training_id", nullable = false)
    private Training training;

    @NotNull
    @OneToOne(optional = false)
    @JoinColumn(name = "image_id", nullable = false)
    private ImageFile imageFile;

    @NotNull
    @Column(name = "image_order", nullable = false)
    private Integer imageOrder;

}
