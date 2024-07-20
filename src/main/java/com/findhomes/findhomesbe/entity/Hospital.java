
package com.findhomes.findhomesbe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="hospitals_tbl")
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
