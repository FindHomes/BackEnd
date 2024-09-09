package com.findhomes.findhomesbe.DTO;

import lombok.Data;

import java.util.List;

@Data
public class ManConRequest {
    private List<String> housingTypes;
    private Prices prices;
    private Region region;

    @Data
    public static class Region {
        private String district;
        private String city;
    }

    @Data
    public static class Prices {
        private int mm;
        private int js;
        private Ws ws;

        @Data
        public static class Ws {
            private int deposit;
            private int rent;
        }
    }

    public String toSentence() {
        StringBuilder sb = new StringBuilder();

        if (!housingTypes.isEmpty()) {
            sb.append("매물 타입: ");
        }
        sb.append(housingTypes.toString());
        if (prices.mm > 0) {
            sb.append("\n매매 가격 최대: ");
            sb.append(prices.mm);
            sb.append("만원");
        }
        if (prices.js > 0) {
            sb.append("\n전세 가격 최대: ");
            sb.append(prices.js);
            sb.append("만원");
        }
        if (prices.ws != null && prices.ws.deposit > 0 && prices.ws.rent > 0) {
            sb.append("\n월세 보증금(deposit) 최대: ");
            sb.append(prices.ws.deposit);
            sb.append("만원");
            sb.append(" / 월세(rent): ");
            sb.append(prices.ws.rent);
            sb.append("만원");
        }
        if (region != null) {
            String temp = "";
            if (region.district != null && !region.district.isEmpty()) {
                temp += region.district;
            }
            if (region.city != null && !region.city.isEmpty()) {
                temp += (" " + region.city);
            }
            if (!temp.isEmpty()) {
                sb.append("\n지역: ");
                sb.append(temp);
            }
        }

        return sb.toString();
    }
}
