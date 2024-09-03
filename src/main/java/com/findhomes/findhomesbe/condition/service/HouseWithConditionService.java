package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.condition.calculate.data.HouseWithCondition;
import com.findhomes.findhomesbe.condition.domain.AllConditions;
import com.findhomes.findhomesbe.condition.domain.PublicData;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.SafetyGrade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HouseWithConditionService {
    private final PublicDataService publicDataService;

    public List<HouseWithCondition> convertHouseList(List<House> houses) {
        return houses.stream().map(this::convertHouse).toList();
    }

    private HouseWithCondition convertHouse(House house) {
        return new HouseWithCondition(house);
    }

    public void injectPublicDataInList(List<HouseWithCondition> houseWithConditions, List
            <AllConditions.PublicConditionData> publicConditionDataList) {
        for (HouseWithCondition houseWithCondition : houseWithConditions) {
            for (AllConditions.PublicConditionData publicConditionData : publicConditionDataList) {
                injectPublicData(houseWithCondition, publicConditionData.getPublicDataEnum());
            }
        }
    }

    private void injectPublicData(HouseWithCondition houseWithCondition, PublicData publicData) {
        String[] districtAndCity = extractDistrictAndCity(houseWithCondition.getHouse().getAddress());
        if (districtAndCity != null) {
            SafetyGrade safetyGrade = publicDataService.getSafetyGradeByAddress(districtAndCity[0], districtAndCity[1]);

            switch (publicData.name()) {
                case "교통사고율":
                    houseWithCondition.getSafetyGradeMap().put(publicData, safetyGrade.getTraffic_accidents());
                    break;
                case "화재율":
                    houseWithCondition.getSafetyGradeMap().put(publicData, safetyGrade.getFire());
                    break;
                case "범죄율":
                    houseWithCondition.getSafetyGradeMap().put(publicData, safetyGrade.getCrime());
                    break;
                case "생활안전":
                    houseWithCondition.getSafetyGradeMap().put(publicData, safetyGrade.getPublic_safety());
                    break;
                case "자살율":
                    houseWithCondition.getSafetyGradeMap().put(publicData, safetyGrade.getSuicide());
                    break;
                case "감염병율":
                    houseWithCondition.getSafetyGradeMap().put(publicData, safetyGrade.getInfectious_diseases());
                    break;
            }
        }
    }

    private String[] extractDistrictAndCity(String address) {
        String[] splitAddress = address.split(address);
        try {
            String district = splitAddress[0];
            if (district.equals("경기도")) {
                district = "경기";
            } else if (district.contains("서울")) {
                district = "서울";
            } else if (district.contains("강원")) {
                district = "강원";
            } else if (district.equals("충청북도")) {
                district = "충북";
            } else if (district.equals("충청남도")) {
                district = "충남";
            } else if (district.equals("전라북도")) {
                district = "전북";
            } else if (district.equals("전라남도")) {
                district = "전남";
            } else if (district.equals("경상북도")) {
                district = "경북";
            } else if (district.equals("경상남도")) {
                district = "경남";
            } else if (district.contains("제주")) {
                district = "제주";
            } else if (district.contains("부산")) {
                district = "부산";
            } else if (district.contains("대구")) {
                district = "대구";
            } else if (district.contains("인천")) {
                district = "인천";
            } else if (district.contains("광주")) {
                district = "광주";
            } else if (district.contains("대전")) {
                district = "대전";
            } else if (district.contains("울산")) {
                district = "울산";
            }
            return new String[]{district, splitAddress[2]};
        } catch (Exception e) {
            // TODO: 예외 처리 제대로 해야됨.
            return null;
        }
    }
}
