package com.findhomes.findhomesbe.specification;

import com.findhomes.findhomesbe.entity.RestaurantIndustry;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class RestaurantIndustrySpecification {
    public static Specification<RestaurantIndustry> containsKeywordInDescription(String keyword) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("placeName"), "%" + keyword + "%");
    }

    public static Specification<RestaurantIndustry> containsKeywordsInDescription(String[] keywords) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            for (String keyword : keywords) {
                predicates.add(criteriaBuilder.like(root.get("placeName"), "%" + keyword + "%"));
            }

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }
}
