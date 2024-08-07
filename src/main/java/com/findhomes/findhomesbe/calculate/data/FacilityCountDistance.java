package com.findhomes.findhomesbe.calculate.data;

import lombok.Data;

@Data
public class FacilityCountDistance {
    private Integer count = 0;
    private Double minDistance = Double.MAX_VALUE;

    public void countUp() {
        count++;
    }

    public void updateMinDistance(Double distance) {
        this.minDistance = Math.min(this.minDistance, distance);
    }
}
