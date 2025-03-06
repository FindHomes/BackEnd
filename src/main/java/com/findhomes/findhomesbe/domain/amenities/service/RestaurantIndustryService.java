package com.findhomes.findhomesbe.domain.amenities.service;

import com.findhomes.findhomesbe.domain.amenities.domain.Amenities;
import com.findhomes.findhomesbe.domain.amenities.domain.RestaurantIndustrySpecification;
import com.findhomes.findhomesbe.domain.amenities.repository.RestaurantAmenitiesRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RestaurantIndustryService {

    private final RestaurantAmenitiesRepository restaurantIndustryRepository;

    public List<Amenities> getRestaurantByKeyword(String kw) {
        return restaurantIndustryRepository.findAll(RestaurantIndustrySpecification.containsKeywordInDescription(kw))
                .stream().map(res -> (Amenities) res).toList();
    }
    public List<Amenities> getRestaurantByKeywords(String[] kws) {
        return restaurantIndustryRepository.findAll(RestaurantIndustrySpecification.containsKeywordsInDescription(kws))
                .stream().map(res -> (Amenities) res).toList();
    }
}
