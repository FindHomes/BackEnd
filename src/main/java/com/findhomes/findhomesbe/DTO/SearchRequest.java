package com.findhomes.findhomesbe.DTO;

import lombok.Data;

import java.util.List;

@Data
public class SearchRequest {
    private ManCon manCon;
    private String userInput;

    @Data
    public static class ManCon {
        private List<String> housingTypes;
        private Prices prices;
        private List<String> regions;

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
    }
}
