package com.findhomes.findhomesbe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "hospital_tbl")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    private String name;

    private String fullAddress;

    private String roadAddress;

    private Integer zipcode;

    private String category;

    private Double x;

    private Double y;

    private String major;

}
