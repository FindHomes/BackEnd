package com.findhomes.findhomesbe.service;

import com.findhomes.findhomesbe.DTO.ManConRequest;
import com.findhomes.findhomesbe.DTO.SearchResponse;
import com.findhomes.findhomesbe.condition.domain.AllConditions;
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

    public List<House> getHouseByAllConditions(AllConditions allConditions) {
        return houseRepository.findAll(HouseSpecification.searchHousesByAllCon(allConditions));
    }

    public SearchResponse.SearchResult makeResponse(List<House> housesSubList) {
        SearchResponse.SearchResult searchResponse = new SearchResponse.SearchResult();
        try {
            searchResponse.setXMin(housesSubList.stream().min(Comparator.comparingDouble(House::getX)).get().getX());
            searchResponse.setXMax(housesSubList.stream().max(Comparator.comparingDouble(House::getX)).get().getX());
            searchResponse.setYMin(housesSubList.stream().min(Comparator.comparingDouble(House::getY)).get().getY());
            searchResponse.setYMax(housesSubList.stream().max(Comparator.comparingDouble(House::getY)).get().getY());
        } catch (NoSuchElementException ignored) {
            log.warn("없음.");
            return null;
        }
        searchResponse.setHouses(housesSubList);
        return searchResponse;
    }
}
