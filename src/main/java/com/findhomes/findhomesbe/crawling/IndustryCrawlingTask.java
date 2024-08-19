package com.findhomes.findhomesbe.crawling;

import com.findhomes.findhomesbe.entity.RestaurantIndustry;
import com.findhomes.findhomesbe.repository.RestaurantIndustryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class IndustryCrawlingTask {
    private static final int MAX_WAIT_TIME = 5;

    private final RestaurantIndustryRepository restaurantIndustryRepository;

    public void exec(int startId, int endId) throws InterruptedException {
        Crawling crawling = new Crawling()
                .setDriverWithShowing()
                .setWaitTime(MAX_WAIT_TIME);
        crawling.openUrl("http://map.naver.com/p/");

        List<String> restaurantName = List.of(
                "백소정 광화문점" + " 서울특별시 종로구 사직로8길 42",
                "커피베이 창경궁점 서울특별시 종로구 율곡로 236",
                "명김밥 서울특별시 종로구 창신길 78",
                "시월책방 서울특별시 종로구 자하문로 83",
                "필린 Fillin 서울특별시 종로구 필운대로1길 12",
                "서촌닭강정공장 서울특별시 종로구 자하문로 6",
                "쭈소바 서울특별시 종로구 사직로8길 4"
        );

        for (int i = startId; i <= endId; i++) {
            /*Optional<Restaurant> restaurantOptional = restaurantIndustryRepository.findById(i);

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

            String restaurantName = restaurant.getPlaceName() + " " + roadAddress;*/
            RestaurantIndustry restaurantIndustry = new RestaurantIndustry();

            WebElement inputElement = crawling.getElementByCssSelector(".input_search");
            if (inputElement == null) {
                System.out.println("[input 못찾음]");
                System.out.println(restaurantIndustry);
                continue;
            }


            inputElement.sendKeys(Keys.CONTROL + "a");
            inputElement.sendKeys(Keys.DELETE);
            inputElement.sendKeys(restaurantName.get(i));
            inputElement.sendKeys(Keys.ENTER);

            crawling.changeIframe("entryIframe");

            boolean flag = false;
            WebElement ele = null;
            WebElement scrollableArea = crawling.getElementByCssSelector("body.place_on_pcmap");
            JavascriptExecutor js = (JavascriptExecutor) crawling.getDriver();
            js.executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", scrollableArea);

            // 블로그 정보 가져오기
            Map<String, Integer> wordCountMap = new HashMap<>();
            List<WebElement> blogElements = crawling.getElementListByCssSelector(".RBifO");
            List<String> allBlogTexts = new ArrayList<>();
            String originalWindow = crawling.getDriver().getWindowHandle();

            if (blogElements != null && !blogElements.isEmpty()) {
                for (WebElement blogElement : blogElements) {
                    // 블로그 요소 클릭 (새 창 열림)
                    blogElement.click();

                    // 새 창으로 전환하기 위해 모든 창 핸들을 가져옴
                    Set<String> allWindows = crawling.getDriver().getWindowHandles();

                    // 새 창 찾기
                    for (String windowHandle : allWindows) {
                        if (!windowHandle.equals(originalWindow)) {
                            crawling.getDriver().switchTo().window(windowHandle); // 새 창으로 포커스 전환
                            break;
                        }
                    }

                    // 새 창에서 텍스트 수집 (원하는 텍스트 선택자를 사용)
                    String blogText = crawling.getElementByCssSelector("body").getText();
                    allBlogTexts.add(blogText); // 텍스트 저장

                    // 창 닫기
                    crawling.getDriver().close();

                    // 원래 창으로 포커스 전환
                    crawling.getDriver().switchTo().window(originalWindow);
                }

                // 블로그에 대해서 텍스트 추출 및 단어 빈도수 계산
                for (String blogText : allBlogTexts) {
                    blogText = blogText.replaceAll("[^가-힣]", " ");
                    String[] words = blogText.split("\\s+"); // 공백으로 단어 분리

                    for (String word : words) {
                        word = word.toLowerCase(); // 소문자로 변환 및 알파벳만 남기기
                        if (!word.isEmpty()) {
                            wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);
                        }
                    }
                }
            }

            // 메뉴 정보
            boolean flag2 = false;
            List<WebElement> menuButton = crawling.getElementListByCssSelector(".tpj9w");
            if (menuButton == null || menuButton.isEmpty()) {
                continue;
            }
            for (WebElement webElement : menuButton) {
                if (webElement.getText().contains("메뉴")) {
                    webElement.click();
                    flag2 = true;
                }
            }
            if (flag2) {
                // 메뉴에 대해서 텍스트 추출 및 단어 빈도수 계산
                List<WebElement> textElements = crawling.getElementListByCssSelector(".MXkFw");
                for (WebElement textElement : textElements) {
                    String text = textElement.getText();
                    text = text.replaceAll("[^가-힣]", " ");
                    String[] words = text.split("\\s+"); // 공백으로 단어 분리

                    for (String word : words) {
                        word = word.toLowerCase(); // 소문자로 변환 및 알파벳만 남기기
                        if (!word.isEmpty()) {
                            wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);
                        }
                    }
                }
            }

            log.info("result: {}", wordCountMap);

            crawling.getDriver().switchTo().defaultContent();
        }

        crawling.quitDriver();
    }
}
