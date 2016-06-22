package java.com.doanduyhai.annotation_processing_demo;

import java.util.UUID;

import com.doanduyhai.annotation_processing_demo.annotation.Column;
import com.doanduyhai.annotation_processing_demo.annotation.Entity;

@Entity
public class ItemEntity {

    @Column
    private UUID id;

    @Column
    private String name;

    @Column
    private Double price;

    @Column
    private Long quantity;
}
