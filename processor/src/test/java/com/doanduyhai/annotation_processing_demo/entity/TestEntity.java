package com.doanduyhai.annotation_processing_demo.entity;

import java.util.UUID;

import com.doanduyhai.annotation_processing_demo.annotation.Column;
import com.doanduyhai.annotation_processing_demo.annotation.Entity;

@Entity
public class TestEntity {

    @Column
    private Long id;

    @Column
    private String name;

    @Column
    private UUID uuid;
}
