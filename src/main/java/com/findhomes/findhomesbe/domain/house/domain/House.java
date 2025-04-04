package com.findhomes.findhomesbe.domain.house.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.geolatte.geom.Point;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private Double longitude; // Not NULL
    private Double latitude; // Not NULL
    private String imgUrl; // Nullable
    @JsonIgnore
    private Point coordinate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status = "ACTIVE";
    private LocalDateTime checkedAt;

    @Transient
    private Integer ranking = 0;
    @Transient
    private transient Double score = 0d; // 직렬화에서 제외됨

    @JsonIgnore
    private transient Double publicDataScore = 0d;
    public void addPublicDataScore(double score) {
        publicDataScore += score;
    }
    @JsonIgnore
    private transient Double facilityDataScore = 0d;
    public void addFacilityDataScore(double score) {
        facilityDataScore += score;
    }

    public void addScore(double score) {
        this.score += score;
    }

    //
    @JsonIgnore
    @OneToMany(mappedBy = "house", cascade = CascadeType.ALL)
    private List<FavoriteHouse> favoriteHouseList;
    @JsonIgnore
    @OneToMany(mappedBy = "house", cascade = CascadeType.ALL)
    private List<RecentlyViewedHouse> recentlyViewedHouseList;

    public House() {

    }

    public House(Integer houseId, String url, String priceType, Integer price, Integer priceForWs, Integer maintenanceFee, String housingType, Boolean isMultiLayer, Boolean isSeparateType, String floor, Double size, Integer roomNum, Integer washroomNum, String direction, LocalDate completionDate, String houseOption, String address, Double longitude, Double latitude, String imgUrl) {
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
        this.longitude = longitude;
        this.latitude = latitude;
        this.imgUrl = imgUrl;
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
                ", houseOption='" + houseOption + '\'' +
                ", address='" + address + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", imgUrl='" + imgUrl + '\'' +
                ", coordinate=" + coordinate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", status='" + status + '\'' +
                ", checkedAt=" + checkedAt +
                '}';
    }
}
