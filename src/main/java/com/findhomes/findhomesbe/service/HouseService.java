package com.findhomes.findhomesbe.service;

import com.findhomes.findhomesbe.DTO.SearchRequest;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.repository.HouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HouseService {
    private final HouseRepository houseRepository;

    public List<House> getHouse(SearchRequest searchRequest) {
        List<House> houseList = houseRepository.findByPriceType("mm");
        for (House house : houseList) {
            System.out.println("house = " + house.toString());
        }
        return null;
    }
}
