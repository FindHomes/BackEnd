package com.findhomes.findhomesbe.service;

import com.findhomes.findhomesbe.DTO.SearchRequest;
import com.findhomes.findhomesbe.DTO.SearchResponse;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.repository.HouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HouseService {
    private final HouseRepository houseRepository;

    public List<House> getHouse(SearchRequest searchRequest) {
        List<House> houseList = houseRepository.findByPriceType("mm");
        for (House house : houseList) {
            System.out.println("house = " + house.toString());
        }
        return houseList;
    }

    public List<SearchResponse.Response.Ranking> convertToRanking(List<House> houses) {
        return houses.stream().map(house -> SearchResponse.Response.Ranking.builder()
                .rank(0) // 여기에 실제 순위를 계산해서 넣어야 합니다.
                .priceType(house.getPriceType())
                .price(house.getPrice())
                .rent(house.getPriceForWs())
                .address(house.getAddress())
                .housingType(house.getHousingType())
                .info(SearchResponse.Response.Ranking.Info.builder()
                        .floor(0) // 여기에 실제 층 정보를 넣어야 합니다.
                        .size(String.valueOf(house.getSize()))
                        .build())
                .build()).collect(Collectors.toList());
    }
}
