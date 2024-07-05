package com.findhomes.findhomesbe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Entity
@Table(name="houses_tbl")
public class House {
    @Id
    private Integer houseId; // Not NULL / 숫자8개
    private String url; // Not NULL / url
    private String priceType; // Not NULL / 매매/전세/월세
    private Integer price; // Not NULL / 만원 단위
    private Integer priceForWs; // Nullable / 만원 단위 / 없으면 NULL
    private Integer maintenanceFee; // Nullable / 만원 단위 / 없으면 NULL
    private String housingType; // Not NULL / 아파트/원룸/투룸/쓰리룸 이상/오피스텔
    private Boolean isMultiLayer; // Not NULL
    private Boolean isSeparateType; // Not NULL
    private String floor; // Not NULL
    private Double size; // Not NULL / 오류가 있을 경우 0
    private Integer roomNum; // Not NULL
    private Integer washroomNum; // Not NULL
    private String direction; // Nullable
    private LocalDate completionDate; // Not NULL
    private String houseOption; // Nullable
    private String address; // Not NULL
    private Double x; // Not NULL
    private Double y; // Not NULL
    private transient Double score; // 직렬화에서 제외됨

    //
    @OneToMany(mappedBy = "house", cascade = CascadeType.ALL)
    private List<SavedRoom> savedRoomList;
    @OneToMany(mappedBy = "house", cascade = CascadeType.ALL)
    private List<ViewedRoom> viewedRoomList;

    public House() {

    }

    public House(Integer houseId, String url, String priceType, Integer price, Integer priceForWs, Integer maintenanceFee, String housingType, Boolean isMultiLayer, Boolean isSeparateType, String floor, Double size, Integer roomNum, Integer washroomNum, String direction, LocalDate completionDate, String houseOption, String address, Double x, Double y) {
        this.houseId = houseId;
        this.url = url;
        this.priceType = priceType;
        this.price = price;
        this.priceForWs = priceForWs;
        this.maintenanceFee = maintenanceFee;
        this.housingType = housingType;
        this.isMultiLayer = isMultiLayer;
        this.isSeparateType = isSeparateType;
        this.floor = floor;
        this.size = size;
        this.roomNum = roomNum;
        this.washroomNum = washroomNum;
        this.direction = direction;
        this.completionDate = completionDate;
        this.houseOption = houseOption;
        this.address = address;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "House{" +
                "houseId=" + houseId +
                ", url='" + url + '\'' +
                ", priceType='" + priceType + '\'' +
                ", price=" + price +
                ", priceForWs=" + priceForWs +
                ", maintenanceFee=" + maintenanceFee +
                ", housingType='" + housingType + '\'' +
                ", isMultiLayer=" + isMultiLayer +
                ", isSeparateType=" + isSeparateType +
                ", floor='" + floor + '\'' +
                ", size=" + size +
                ", roomNum=" + roomNum +
                ", washroomNum=" + washroomNum +
                ", direction='" + direction + '\'' +
                ", completionDate=" + completionDate +
                ", option='" + houseOption + '\'' +
                ", address='" + address + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", score=" + score +
                '}';
    }
}
