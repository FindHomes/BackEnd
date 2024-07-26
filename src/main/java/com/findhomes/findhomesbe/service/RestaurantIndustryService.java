package com.findhomes.findhomesbe.service;

import com.findhomes.findhomesbe.entity.Restaurant;
import com.findhomes.findhomesbe.repository.RestaurantIndustryRepository;
import com.findhomes.findhomesbe.specification.RestaurantIndustrySpecification;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RestaurantIndustryService {

    private final RestaurantIndustryRepository restaurantIndustryRepository;

    public List<Restaurant> getRestaurantByKeyword(String[] kws) {
        return restaurantIndustryRepository.findAll(RestaurantIndustrySpecification.containsKeywordsInDescription(kws));
    }
}
