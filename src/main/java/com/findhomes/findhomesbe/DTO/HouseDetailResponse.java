package com.findhomes.findhomesbe.DTO;

import com.findhomes.findhomesbe.entity.House;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HouseDetailResponse {
    private Boolean success;
    private Integer code;
    private String message;
    private ResponseHouse result;

    public HouseDetailResponse(House house, boolean isFavorite, Boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.result = new ResponseHouse(house,isFavorite);
    }

    @Data
    public static class ResponseHouse {
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
        private List<String> imgUrl; // Nullable
        private Double score = 0d;
        private boolean isFavorite;
        public ResponseHouse(House house, Boolean isFavorite) {
            this.houseId = house.getHouseId();
            this.url = house.getUrl();
            this.priceType = house.getPriceType();
            this.price = house.getPrice();
            this.priceForWs = house.getPriceForWs();
            this.maintenanceFee = house.getMaintenanceFee();
            this.housingType = house.getHousingType();
            this.isMultiLayer = house.getIsMultiLayer();
            this.isSeparateType = house.getIsSeparateType();
            this.floor = house.getFloor();
            this.size = house.getSize();
            this.roomNum = house.getRoomNum();
            this.washroomNum = house.getWashroomNum();
            this.direction = house.getDirection();
            this.completionDate = house.getCompletionDate();
            this.houseOption = house.getHouseOption();
            this.address = house.getAddress();
            this.x = house.getLongitude();
            this.y = house.getLatitude();
            this.imgUrl = house.getImgUrl() == null || house.getImgUrl().isEmpty() ? new ArrayList<>() : List.of(house.getImgUrl().split("@@@"));
            this.score = house.getScore();
            this.isFavorite = isFavorite;
        }
    }
}
