package com.findhomes.findhomesbe.domain.crawling;

import com.findhomes.findhomesbe.domain.amenities.domain.RestaurantAmenities;
import com.findhomes.findhomesbe.domain.amenities.repository.RestaurantAmenitiesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.findhomes.findhomesbe.domain.crawling.CrawlingConst.MAX_WAIT_TIME;

@Component
@RequiredArgsConstructor
@Slf4j
public class IndustryCrawlingTask {
    private static final String url = "http://map.naver.com/p/";
    private static final int LAST_RESTAURANT_ID = 682391;
    private static List<String > BAN_WORDS = List.of("식사", "있는", "단품", "디너", "런치", "점심", "주는", "할인", "인기", "대표", "인기대표", "메뉴", "맛있는", "변동", "만든", "입니다", "맛을");

    private final RestaurantAmenitiesRepository restaurantIndustryRepository;

    public void exec(String start) {
        List<Integer> startIndexArray = Arrays.stream(start.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        int threadCount = startIndexArray.size();

        // CompletableFuture 리스트 생성
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            int startId = startIndexArray.get(i);
            int endId = (i == threadCount - 1) ? LAST_RESTAURANT_ID : startIndexArray.get(i + 1) - 1;

            // 비동기 작업 추가
            futures.add(runAsync(startId, endId));
        }

        // 모든 비동기 작업이 끝날 때까지 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private CompletableFuture<Void> runAsync(int start, int end) {
        return CompletableFuture.runAsync(() -> {
            execOneTask(start, end);
        });
    }

    public void execOneTask(int startId, int endId) {
        Crawling crawling = new Crawling()
                .setDriver(true, false)
                .setWaitTime(MAX_WAIT_TIME);
        crawling.openUrl(url);

        for (int i = startId; i <= endId; i++) {
            if (i > 0 && i % 7 == 0) {
                crawling.openUrlNewTab(url);
            } else if (i > 0 && i % 200 == 0) {
                crawling.quitDriver();
                crawling = new Crawling()
                        .setDriver(true, false)
                        .setWaitTime(MAX_WAIT_TIME);
                crawling.openUrl(url);
            }

            Optional<RestaurantAmenities> restaurantOptional = restaurantIndustryRepository.findById(i);

            if (restaurantOptional.isEmpty()) {
                log.error("[[menu - thread {} - id {} restaurant not found]]", Thread.currentThread().threadId(), i);
                continue;
            }
            RestaurantAmenities restaurant = restaurantOptional.get();
            String roadAddress = restaurant.getRoadAddress();
            if (roadAddress.contains(",")) {
                roadAddress = roadAddress.split(",")[0];
            } else if (roadAddress.contains("(")) {
                roadAddress = roadAddress.split("\\(")[0];
            }

            String restaurantName = restaurant.getPlaceName() + " " + roadAddress;

            WebElement inputElement = crawling.getElementByCssSelector(".input_search");
            if (inputElement == null) {
                log.error("[[menu - thread {} - id {} restaurant input not found]]", Thread.currentThread().threadId(), i);
                continue;
            }


            inputElement.sendKeys(Keys.CONTROL + "a");
            inputElement.sendKeys(Keys.DELETE);
            inputElement.sendKeys(restaurantName);
            inputElement.sendKeys(Keys.ENTER);

            try {
                crawling.changeIframe("entryIframe");
            } catch (Exception e) {
                log.error("[[menu - thread {} - id {} restaurant entryIframe not found]]", Thread.currentThread().threadId(), i);
                continue;
            }

//            boolean flag = false;
//            WebElement ele = null;
//            WebElement scrollableArea = crawling.getElementByCssSelector("body.place_on_pcmap");
//            JavascriptExecutor js = (JavascriptExecutor) crawling.getDriver();
//            js.executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", scrollableArea);

            // 음식점 종류
            String cuisine = "";
            WebElement cuisineEl = crawling.getElementByCssSelector(".lnJFt");
            if (cuisineEl != null) {
                cuisine = cuisineEl.getText();
            }
            // 메뉴 정보
            boolean flag2 = false;
            List<WebElement> menuButton = crawling.getElementListByCssSelector(".tpj9w");
            if (menuButton == null || menuButton.isEmpty()) {
                log.info("[[menu - thread {} - id {} restaurant tab not found]]", Thread.currentThread().threadId(), i);
                // entryIframe에서 원래로 돌아오게 하는 코드
                crawling.getDriver().switchTo().defaultContent();
                saveRestaurant(restaurant, cuisine);
                continue;
            }
            try {
                for (WebElement webElement : menuButton) {
                    if (webElement.getText().contains("메뉴")) {
                        webElement.click();
                        flag2 = true;
                    }
                }
            } catch (Exception e) { // 탭 선택자가 다를 경우
                List<WebElement> menuButton2 = crawling.getElementListByCssSelector(".tab");
                if (menuButton2 == null || menuButton2.isEmpty()) {
                    log.info("[[menu - thread {} - id {} restaurant tab not found]]", Thread.currentThread().threadId(), i);
                    // entryIframe에서 원래로 돌아오게 하는 코드
                    crawling.getDriver().switchTo().defaultContent();
                    saveRestaurant(restaurant, cuisine);
                    continue;
                }
                for (WebElement webElement : menuButton2) {
                    if (webElement.getText().contains("메뉴")) {
                        webElement.click();
                        flag2 = true;
                    }
                }
            }
            // 단어 추출
            Map<String, Integer> wordCountMap = new HashMap<>();
            if (flag2) {
                // 메뉴에 대해서 텍스트 추출 및 단어 빈도수 계산
                List<WebElement> textElements = crawling.getElementListByCssSelector(".MXkFw");
                if (textElements == null || textElements.isEmpty()) {
                    textElements = crawling.getElementListByCssSelector("div.info_detail > div.tit");
                }
                if (textElements == null || textElements.isEmpty()) {
                    log.info("[[menu - thread {} - id {} restaurant menu info not found]]", Thread.currentThread().threadId(), i);
                    // entryIframe에서 원래로 돌아오게 하는 코드
                    crawling.getDriver().switchTo().defaultContent();
                    saveRestaurant(restaurant, cuisine);
                    continue;
                }
                for (WebElement textElement : textElements) {
                    String text = textElement.getText();
                    text = text.replaceAll("[^가-힣]", " ");
                    String[] words = text.split("\\s+"); // 공백으로 단어 분리

                    for (String word : words) {
                        if (word.length() > 1 && !BAN_WORDS.contains(word)) {
                            wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);
                        }
                    }
                }
            } else {
                log.info("[[menu - thread {} - id {} restaurant name \"menu\" tab not found]]", Thread.currentThread().threadId(), i);
                // entryIframe에서 원래로 돌아오게 하는 코드
                crawling.getDriver().switchTo().defaultContent();
                saveRestaurant(restaurant, cuisine);
                continue;
            }

            if (wordCountMap.isEmpty()) {
                log.info("[[menu - thread {} - id {} restaurant no menu not found]]", Thread.currentThread().threadId(), i);
                // entryIframe에서 원래로 돌아오게 하는 코드
                crawling.getDriver().switchTo().defaultContent();
                saveRestaurant(restaurant, cuisine);
                continue;
            }

            List<Map.Entry<String, Integer>> topNWords = getTopNWords(wordCountMap, 7);
            log.info("[[menu - thread {} - id {} restaurant result: {}]]", Thread.currentThread().threadId(), i, topNWords);

            String tag = cuisine + "," +
                    topNWords.stream()
                    .map(Map.Entry::getKey)
                    .collect(Collectors.joining(","));

            saveRestaurant(restaurant, additionalPostprocessing(tag));

            // entryIframe에서 원래로 돌아오게 하는 코드
            crawling.getDriver().switchTo().defaultContent();
        }

        crawling.quitDriver();
    }

    private void saveRestaurant(RestaurantAmenities restaurantIndustry, String tag) {
        restaurantIndustry.setPlaceTags(tag);
        restaurantIndustryRepository.save(restaurantIndustry);
    }

    private List<Map.Entry<String, Integer>> getTopNWords(Map<String, Integer> map, int n) {
        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(map.entrySet());
        entryList.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        return entryList.subList(0, Math.min(n, entryList.size()));
    }

    private String additionalPostprocessing(String tag) {
        if (tag.contains("커피") || tag.contains("아메리카노") || tag.contains("라떼")) {
            return tag + ",카페";
        }
        if (tag.contains("버거")) {
            return tag + ",햄버거,패스트푸드";
        }
        if (tag.contains("피자")) {
            return tag + ",패스트푸드";
        }
        return tag;
    }
}
