package com.findhomes.findhomesbe.specification;

import com.findhomes.findhomesbe.DTO.SearchRequest;
import com.findhomes.findhomesbe.entity.House;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class HouseSpecification {
    public static Specification<House> searchHousesByManCon(SearchRequest.ManCon manCon) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (manCon != null) {
                // Housing Type 조건
                if (manCon.getHousingTypes() != null && !manCon.getHousingTypes().isEmpty()) {
                    predicates.add(root.get("housingType").in(manCon.getHousingTypes()));
                }

                // Price 조건
                if (manCon.getPrices() != null) {
                    SearchRequest.ManCon.Prices prices = manCon.getPrices();

                    // 매매 조건
                    predicates.add(criteriaBuilder.or(
                            criteriaBuilder.and(
                                    criteriaBuilder.equal(root.get("priceType"), "매매"),
                                    criteriaBuilder.lessThanOrEqualTo(root.get("price"), prices.getMm())
                            ),
                            // 전세 조건
                            criteriaBuilder.and(
                                    criteriaBuilder.equal(root.get("priceType"), "전세"),
                                    criteriaBuilder.lessThanOrEqualTo(root.get("price"), prices.getJs())
                            ),
                            // 월세 조건
                            criteriaBuilder.and(
                                    criteriaBuilder.equal(root.get("priceType"), "월세"),
                                    criteriaBuilder.lessThanOrEqualTo(root.get("price"), prices.getWs().getDeposit()),
                                    criteriaBuilder.lessThanOrEqualTo(root.get("priceForWs"), prices.getWs().getRent())
                            )
                    ));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
