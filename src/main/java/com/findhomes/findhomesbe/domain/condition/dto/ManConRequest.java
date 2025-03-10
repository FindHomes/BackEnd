package com.findhomes.findhomesbe.domain.condition.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ManConRequest {
    private List<String> housingTypes;
    private Prices prices;
    private Region region;

    public String typeInfoToString() {
        return String.join(", ", housingTypes);
    }

    @Data
    public static class Region {
        private String district;
        private String city;

        @Override
        public String toString() {
            return district + " " + city;
        }
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

        @Override
        public String toString() {
            List<String> result = new ArrayList<>();
            if (mm > 0) {
                if (mm > 200000) {
                    result.add("매매가 무제한");
                } else {
                    result.add("매매가 최대: " + mm + "만원");
                }
            }
            if (js > 0) {
                if (js > 100000) {
                    result.add("전세가 무제한");
                } else {
                    result.add("전세가 최대: " + js + "만원");
                }
            }
            if (ws != null && ws.deposit > 0 && ws.rent > 0) {
                String wsStr = "";
                if (ws.deposit > 100000) {
                    wsStr += "월세 보증금 무제한";
                } else {
                    wsStr += "월세 보증금 최대: " + ws.deposit + "만원";
                }
                if (ws.rent > 1000) {
                    wsStr += " & 월세 무제한";
                } else {
                    wsStr += " & 월세 최대" + ws.rent + "만원";
                }
                result.add(wsStr);
            }
            return String.join(" / ", result);
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
