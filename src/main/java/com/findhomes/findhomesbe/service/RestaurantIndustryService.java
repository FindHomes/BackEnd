package com.findhomes.findhomesbe.service;

import com.findhomes.findhomesbe.entity.Industry;
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

    public List<Industry> getRestaurantByKeyword(String kw) {
        return restaurantIndustryRepository.findAll(RestaurantIndustrySpecification.containsKeywordInDescription(kw))
                .stream().map(res -> (Industry) res).toList();
    }
    public List<Industry> getRestaurantByKeywords(String[] kws) {
        return restaurantIndustryRepository.findAll(RestaurantIndustrySpecification.containsKeywordsInDescription(kws))
                .stream().map(res -> (Industry) res).toList();
    }
}
