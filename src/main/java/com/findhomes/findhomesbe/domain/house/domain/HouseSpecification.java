package com.findhomes.findhomesbe.domain.house.domain;

import com.findhomes.findhomesbe.domain.condition.dto.ManConRequest;
import com.findhomes.findhomesbe.domain.condition.domain.AllConditions;
import com.findhomes.findhomesbe.domain.condition.domain.HouseCondition;
import com.findhomes.findhomesbe.domain.condition.domain.HouseOption;
import com.findhomes.findhomesbe.domain.amenities.repository.RegionsRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component // 빈으로 등록해서 관리함 (regions 리포지터리 사용하기 위함)
public class HouseSpecification {
    @Autowired
    private RegionsRepository regionsRepository;
    public Specification<House> searchHousesByAllCon(AllConditions allConditions) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

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
            List<HouseOption> houseOptionList = allConditions.getHouseOptionDataList().stream().map(AllConditions.HouseOptionData::getOption).toList();
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
