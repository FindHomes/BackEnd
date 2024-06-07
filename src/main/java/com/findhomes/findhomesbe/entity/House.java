package com.findhomes.findhomesbe.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Entity
@Table(name="houses_tbl")
public class House {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer houseId;
    private String priceType;
    private Integer price;
    private Integer priceForWS;
    private String housingType;
    private Float size;
    private Integer roomNum;
    private Integer washroomNum;
    private String address;
    private Double x;
    private Double y;
    private transient Double score; // 직렬화에서 제외됨

    public House() {

    }

    @Override
    public String toString() {
        return "House{" +
                "houseId=" + houseId +
                ", priceType='" + priceType + '\'' +
                ", price=" + price +
                ", priceForWS=" + priceForWS +
                ", housingType='" + housingType + '\'' +
                ", size=" + size +
                ", roomNum=" + roomNum +
                ", washroomNum=" + washroomNum +
                ", address='" + address + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", score=" + score +
                '}';
    }
}
