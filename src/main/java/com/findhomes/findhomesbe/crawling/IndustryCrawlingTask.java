package com.findhomes.findhomesbe.crawling;

import com.findhomes.findhomesbe.entity.Restaurant;
import com.findhomes.findhomesbe.repository.RestaurantIndustryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class IndustryCrawlingTask {
    private static final int MAX_WAIT_TIME = 5;

    private final RestaurantIndustryRepository restaurantIndustryRepository;

    public void exec(int startId, int endId) {
        Crawling crawling = new Crawling()
                .setDriverWithShowing()
                .setWaitTime(MAX_WAIT_TIME);
        crawling.openUrl("http://map.naver.com/p/");

        for (int i = startId; i <= endId; i++) {
            Optional<Restaurant> restaurantOptional = restaurantIndustryRepository.findById(i);

            if (restaurantOptional.isEmpty()) {
                continue;
            }
            Restaurant restaurant = restaurantOptional.get();
            String roadAddress = restaurant.getRoadAddress();
            if (roadAddress.contains(",")) {
                roadAddress = roadAddress.split(",")[0];
            } else if (roadAddress.contains("(")) {
                roadAddress = roadAddress.split("\\(")[0];
            }


            WebElement inputElement = crawling.getElementByCssSelector(".input_search");
            if (inputElement == null) {
                System.out.println("[input 못찾음]");
                System.out.println(restaurant);
                continue;
            }

            String restaurantName = restaurant.getPlace_name() + " " + roadAddress;

            inputElement.sendKeys(Keys.CONTROL + "a");
            inputElement.sendKeys(Keys.DELETE);
            inputElement.sendKeys(restaurantName);
            inputElement.sendKeys(Keys.ENTER);

            // 식당 정보 가져오기
            // 스크롤 내리기
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                continue;
            }
            WebElement scrollableElement = crawling.getElementByCssSelector(".GHAhO");
            if (scrollableElement == null) {
                System.out.println("[식당 검색이 안됨]");
                System.out.println(restaurant);
                continue;
            }
            try {
                Thread.sleep(200);
                scrollableElement.sendKeys(Keys.PAGE_DOWN);
                Thread.sleep(200);
                scrollableElement.sendKeys(Keys.PAGE_DOWN);
                Thread.sleep(200);
                scrollableElement.sendKeys(Keys.PAGE_DOWN);
                Thread.sleep(200);
                scrollableElement.sendKeys(Keys.PAGE_DOWN);
            } catch (InterruptedException e) {
                continue;
            }
            // Thread.sleep(1000);
            List<WebElement> blogElementList = crawling.getElementListByCssSelector(".RHxFw");

            // 블로그 글이 없을 경우
            if (blogElementList == null || blogElementList.isEmpty()) {
                System.out.println("[다음 식당 블로그 글이 없음]");
                System.out.println(restaurant);
            }

            for (WebElement webElement : blogElementList) {
                webElement.click();
            }
            // 텍스트 추출 및 단어 빈도수 계산
            Map<String, Integer> wordCountMap = new HashMap<>();
            ArrayList<String> tabs = new ArrayList<>(crawling.getDriver().getWindowHandles());
            for (int j = blogElementList.size(); j > 0; j--) {
                // 새로운 탭으로 전환
                crawling.getDriver().switchTo().window(tabs.get(j));

                String pageSource = crawling.getDriver().getPageSource();
                Document doc = Jsoup.parse(pageSource);
                Elements elements = doc.body().select("*");

                for (Element element : elements) {
                    String text = element.text();
                    String[] words = text.split("\\s+"); // 공백으로 단어 분리

                    for (String word : words) {
                        word = word.toLowerCase(); // 소문자로 변환 및 알파벳만 남기기
                        if (!word.isEmpty()) {
                            wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);
                        }
                    }
                }

                crawling.getDriver().close();
            }
            // 원래 탭으로 전환
            crawling.getDriver().switchTo().window(tabs.get(0));

            List<String> mostCommonWords = wordCountMap.entrySet().stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .limit(5)
                    .map(Map.Entry::getKey)
                    .toList();

            restaurant.setPlaceTags(String.join(",", mostCommonWords));
            System.out.println("[업데이트 완료]");
            System.out.println(restaurant);
            restaurantIndustryRepository.save(restaurant);
        }

        crawling.closeDriver();
    }
}
