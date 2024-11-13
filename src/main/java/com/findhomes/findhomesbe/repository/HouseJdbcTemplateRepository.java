package com.findhomes.findhomesbe.repository;

import com.findhomes.findhomesbe.DTO.ManConRequest;
import com.findhomes.findhomesbe.condition.domain.AllConditions;
import com.findhomes.findhomesbe.condition.domain.HouseCondition;
import com.findhomes.findhomesbe.condition.domain.HouseOption;
import com.findhomes.findhomesbe.entity.House;
import org.geolatte.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

@Repository
public class HouseJdbcTemplateRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<House> searchHousesByAllCon(AllConditions allConditions) {
        // 기본 SQL 시작
        StringBuilder sql = new StringBuilder("SELECT h.* FROM houses_tbl AS h, regions_tbl AS rg WHERE h.status = 'ACTIVE' ");
        List<Object> params = new ArrayList<>();

        // 필수 조건 추가
        ManConRequest manConRequest = allConditions.getManConRequest();

        if (manConRequest != null) {
            // 지역 조건 추가
            String cityName = manConRequest.getRegion().getCity();  // 예시로 도시 이름을 가져옴
            String districtName = manConRequest.getRegion().getDistrict();  // 예시로 구 이름을 가져옴

            sql.append("AND rg.city = ? ");
            params.add(cityName);
            sql.append("AND rg.district = ? ");
            params.add(districtName);
            sql.append("AND ST_Contains(rg.boundary, h.coordinate) ");

            // Housing Type 조건
            if (manConRequest.getHousingTypes() != null && !manConRequest.getHousingTypes().isEmpty()) {
                sql.append("AND h.housing_type IN (");
                for (int i = 0; i < manConRequest.getHousingTypes().size(); i++) {
                    sql.append("?");
                    if (i < manConRequest.getHousingTypes().size() - 1) {
                        sql.append(", ");
                    }
                    params.add(manConRequest.getHousingTypes().get(i));
                }
                sql.append(") ");
            }

            // Price 조건
            if (manConRequest.getPrices() != null) {
                ManConRequest.Prices prices = manConRequest.getPrices();

                sql.append("AND (");

                // 매매 조건
                sql.append("(h.price_type = '매매' AND h.price <= ?) ");
                params.add(prices.getMm());

                // 전세 조건
                sql.append("OR (h.price_type = '전세' AND h.price <= ?) ");
                params.add(prices.getJs());

                // 월세 조건
                sql.append("OR (h.price_type = '월세' AND h.price <= ? AND h.price_for_ws <= ?) ");
                params.add(prices.getWs().getDeposit());
                params.add(prices.getWs().getRent());

                sql.append(") ");
            }
        }

        // 매물 추가 조건
        List<AllConditions.HouseConditionData> houseConditionDataList = allConditions.getHouseConditionDataList();
        if (houseConditionDataList != null && !houseConditionDataList.isEmpty()) {
            for (AllConditions.HouseConditionData houseConditionData : houseConditionDataList) {
                HouseCondition condition = houseConditionData.getHouseConditionEnum();
                Object value = houseConditionData.getValue();

                if (value != null) {
                    // SQL 구문에 조건을 추가하고 값도 함께 설정
                    sql.append("AND ").append(condition.jdbcTemplateFunction.apply(value)).append(" ");
                }
            }
        }

        // 매물 옵션 조건
        List<HouseOption> houseOptionList = allConditions.getHouseOptionDataList().stream().map(AllConditions.HouseOptionData::getOption).toList();
        if (houseOptionList != null && !houseOptionList.isEmpty()) {
            sql.append("AND (");
            for (int i = 0; i < houseOptionList.size(); i++) {
                sql.append("h.house_option LIKE ? ");
                params.add("%" + houseOptionList.get(i).getHouseOption() + "%");
                if (i < houseOptionList.size() - 1) {
                    sql.append("AND ");
                }
            }
            sql.append(") ");
        }

        // 결과 쿼리 실행
        return jdbcTemplate.query(sql.toString(), params.toArray(), new BeanPropertyRowMapper<>(House.class));
    }
}
