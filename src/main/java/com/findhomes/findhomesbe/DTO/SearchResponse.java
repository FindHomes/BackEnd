package com.findhomes.findhomesbe.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchResponse {
    private Response response;

    @Data
    @Builder
    public static class Response {
        private List<Ranking> rankings;

        @Data
        @Builder
        public static class Ranking {
            private int rank;
            private String priceType;
            private int price;
            private int rent;
            private String address;
            private String housingType;
            private Info info;

            @Data
            @Builder
            public static class Info {
                private int floor;
                private String size;
            }
        }
    }
}
