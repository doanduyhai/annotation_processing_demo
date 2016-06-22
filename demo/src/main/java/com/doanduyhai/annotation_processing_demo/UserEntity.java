package java.com.doanduyhai.annotation_processing_demo;

import java.util.Date;

import com.doanduyhai.annotation_processing_demo.annotation.Column;
import com.doanduyhai.annotation_processing_demo.annotation.Entity;

@Entity
public class UserEntity {

    @Column
    private Long id;

    @Column
    private String firstname;

    @Column
    private String lastname;

    @Column
    private Date birthDate;
}
