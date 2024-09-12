package com.findhomes.findhomesbe.specification;

import com.findhomes.findhomesbe.DTO.ManConRequest;
import com.findhomes.findhomesbe.condition.domain.AllConditions;
import com.findhomes.findhomesbe.condition.domain.HouseCondition;
import com.findhomes.findhomesbe.condition.domain.HouseOption;
import com.findhomes.findhomesbe.entity.House;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class HouseSpecification {
    public static Specification<House> searchHousesByAllCon(AllConditions allConditions) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Polygon 인덱싱을 사용하여 지역 내 House 검색
//            Predicate withinPolygon = criteriaBuilder.isTrue(
//                    criteriaBuilder.function("ST_Within", Boolean.class, root.get("location"), criteriaBuilder.literal(polygon))
//            );
//            predicates.add(withinPolygon);
            // 필수 조건 추가
            ManConRequest manConRequest = allConditions.getManConRequest();
            if (manConRequest != null) {
                // Housing Type 조건
                if (manConRequest.getHousingTypes() != null && !manConRequest.getHousingTypes().isEmpty()) {
                    predicates.add(root.get("housingType").in(manConRequest.getHousingTypes()));
                }

                // Price 조건
                if (manConRequest.getPrices() != null) {
                    ManConRequest.Prices prices = manConRequest.getPrices();

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

            // 매물 추가 조건 추가
            List<AllConditions.HouseConditionData> houseConditionDataList = allConditions.getHouseConditionDataList();
            if (houseConditionDataList != null && !houseConditionDataList.isEmpty()) {
                for (AllConditions.HouseConditionData houseConditionData : houseConditionDataList) {
                    HouseCondition condition = houseConditionData.getHouseConditionEnum();
                    Object value = houseConditionData.getValue();

                    if (value != null) {
                        // 각 조건에 따른 Predicate 생성
                        Predicate predicate = condition.buildPredicate(criteriaBuilder, root, value);
                        predicates.add(predicate);
                    }
                }
            }

            // 매물 옵션 조건 추가
            List<HouseOption> houseOptionList = allConditions.getHouseOptionList();
            if (houseOptionList != null && !houseOptionList.isEmpty()) {
                for (HouseOption option : houseOptionList) {
                    String optionStr = option.getHouseOption();
                    predicates.add(criteriaBuilder.like(root.get("houseOption"), "%" + optionStr + "%"));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
