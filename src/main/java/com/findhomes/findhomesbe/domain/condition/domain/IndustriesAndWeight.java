package com.findhomes.findhomesbe.domain.condition.domain;

import com.findhomes.findhomesbe.domain.amenities.domain.Amenities;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class IndustriesAndWeight {
    private List<Amenities> industries;
    private Integer weight;
    private Double maxRadius;

    private AllConditions.FacilityConditionData facilityConditionData;
}
