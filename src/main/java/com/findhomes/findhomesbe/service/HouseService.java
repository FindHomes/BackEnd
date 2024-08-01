package com.findhomes.findhomesbe.service;

import com.findhomes.findhomesbe.DTO.ManConRequest;
import com.findhomes.findhomesbe.DTO.SearchResponse;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.repository.HouseRepository;
import com.findhomes.findhomesbe.specification.HouseSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HouseService {
    private final HouseRepository houseRepository;

    public List<House> getHouse(ManConRequest searchRequest) {
//        List<House> houseList = houseRepository.findByPriceType("mm");
        List<House> houseList = houseRepository.findAll();

//        for (House house : houseList) {
//            System.out.println("house = " + house.toString());
//        }
        return houseList;
    }


    public List<House> getManConHouses(ManConRequest manConRequest) {
        return houseRepository.findAll(HouseSpecification.searchHousesByManCon(manConRequest));
    }

    public List<House> filterByUserInput(Map<String, String> condition, List<House> houses) {
        // 관리비, 복층, 분리형, 층수, 크기, 방 수, 화장실 수, 방향, 완공일, 옵션
        Object[] dataParsing = new Object[]{0, false, false, 0, 0, 0, 0, "동", LocalDate.now(), ""};
        // 조건 전처리
        for (Map.Entry<String, String> entry : condition.entrySet()) {
            switch (entry.getKey()) {
                case "관리비":
                    dataParsing[0] = entry.getValue();
                    break;
                case "복층":
                    dataParsing[1] = entry.getValue();
                    break;
                case "분리형":
                    dataParsing[2] = entry.getValue();
                    break;
                case "층수":
                    dataParsing[3] = toInteger(entry.getValue());
                    break;
                case "크기":
                    dataParsing[4] = entry.getValue();
                    break;
                case "방 수":
                    dataParsing[5] = toInteger(entry.getValue());
                    break;
                case "화장실 수":
                    dataParsing[6] = toInteger(entry.getValue());
                    break;
                case "방향":
                    dataParsing[7] = entry.getValue();
                    break;
                case "완공일":
                    dataParsing[8] = entry.getValue();
                    break;
                case "옵션":
                    dataParsing[9] = entry.getValue();
                    break;
            }
        }

        // 매물 필터링
        return houses.stream()
                .filter(house ->
                    house.getRoomNum() >= (Integer) dataParsing[5]
                        && house.getWashroomNum() >= (Integer) dataParsing[6]
                )
                .toList();
    }

    private static int toInteger(String value) {
        String floorStr = value.replaceAll("[^0-9]", "");
        return floorStr.isEmpty() ? -1 : Integer.parseInt(floorStr);
    }

    public SearchResponse makeResponse(List<House> housesSubList) {
        SearchResponse searchResponse = new SearchResponse();
        try {
            searchResponse.setXMin(housesSubList.stream().min(Comparator.comparingDouble(House::getX)).get().getX());
            searchResponse.setXMax(housesSubList.stream().max(Comparator.comparingDouble(House::getX)).get().getX());
            searchResponse.setYMin(housesSubList.stream().min(Comparator.comparingDouble(House::getY)).get().getY());
            searchResponse.setYMax(housesSubList.stream().max(Comparator.comparingDouble(House::getY)).get().getY());
        } catch (NoSuchElementException ignored) {
            log.warn("아니 이거 발생하면 안되는데...;;");
            return null;
        }
        searchResponse.setHouses(housesSubList);
        return searchResponse;
    }
}
