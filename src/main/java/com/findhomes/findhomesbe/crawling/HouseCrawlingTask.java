package com.findhomes.findhomesbe.crawling;

import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.exception.exception.DataNotFoundException;
import com.findhomes.findhomesbe.service.HouseService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.openqa.selenium.*;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.findhomes.findhomesbe.crawling.CrawlingConst.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class HouseCrawlingTask {
    private final HouseService houseService;

    private Crawling oneTwoCrawling;
    private Crawling aptCrawling;
    private Crawling houseCrawling;
    private Crawling officeCrawling;

    public void exec() throws InterruptedException {
        // CompletableFuture로 비동기 병렬처리
        CompletableFuture.allOf(
                runAsync(shuffledOneTwoUrls, oneTwoCrawling),
                runAsync(shuffledAptUrls, aptCrawling),
                runAsync(shuffledHouseUrls, houseCrawling),
                runAsync(shuffledOfficeUrls, officeCrawling)
        ).join(); // 모든 비동기 작업이 종료될 때까지 부모 대기
    }

    @PreDestroy
    public void closeChrome() {
        oneTwoCrawling.quitDriver();
        aptCrawling.quitDriver();
        houseCrawling.quitDriver();
        officeCrawling.quitDriver();
    }

    private CompletableFuture<Void> runAsync(Supplier<List<String>> supplier, Crawling crawling) {
        List<String> urls = supplier.get();
        return CompletableFuture.runAsync(() -> {
            try {
                execOne(supplier.get(), crawling);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void execOne(List<String> urls, Crawling mainCrawling) throws InterruptedException {
        Integer count = 0;
        mainCrawling = new Crawling()
                .setDriver(false, true)
                .setWaitTime(MAX_WAIT_TIME);
        mainCrawling.getDriver().manage().window().maximize();
        for (int i = 0; i < urls.size(); i++) {
            try {
                if (i != 0 && i % 5 == 0) {
                    mainCrawling.quitDriver();
                    mainCrawling = new Crawling()
                            .setDriver(false, true)
                            .setWaitTime(MAX_WAIT_TIME);
                }
                String url = urls.get(i);
                log.info("[[{} thread - {} index start]]", Thread.currentThread().threadId(), i);
                mainCrawling.openUrlNewTab(url);

                int nextPageCount = 0;

                while (true) {
                    List<WebElement> salesElementList = mainCrawling.getElementListByCssSelector(saleElSelector);
                    if (salesElementList == null) {
                        log.info("[[{} thread - no item in url: \"{}\"]]", Thread.currentThread().threadId(), url);
                        break;
                    }
                    for (WebElement element : salesElementList) {
                        mainCrawling.getProxy().newHar("new_har");

                        try {
                            // 클릭 시도
                            element.click();
                        } catch (ElementClickInterceptedException e) {
                            // 예외 발생 시 JavaScript로 강제 클릭
                            JavascriptExecutor jsExecutor = (JavascriptExecutor) mainCrawling.getDriver();
                            jsExecutor.executeScript("arguments[0].click();", element);
                        }
//
                        // Wait for the page to load (you can add explicit waits here if necessary)
                        Thread.sleep(1500);

                        List<String> img_urls = new ArrayList<>();
                        // Get the HAR data and extract network requests
                        Har har = mainCrawling.getProxy().getHar();
                        List<HarEntry> entries = har.getLog().getEntries();

                        // Print all requests, filtering by "cloudfront" if needed
                        for (HarEntry entry : entries) {
                            String requestUrl = entry.getRequest().getUrl();
                            if (requestUrl.contains("cloudfront")) {
                                img_urls.add(requestUrl);
                            }
                        }

                        try {
                            postProcessing(mainCrawling, img_urls, count);
                        } catch (Exception e) {
                            log.error("[[[failed url: {}]]]", url, e);
                        } finally {
                            img_urls.clear();

                            Har har2 = mainCrawling.getProxy().getHar();
                            har2 = null;
                        }
                    }

                    // 다음 버튼
                    List<WebElement> nextButtonList = mainCrawling.getElementListByCssSelector(nextBtnSelector);
                    if (nextButtonList != null) {
                        WebElement nextButton = nextButtonList.get(nextButtonList.size() - 1);
                        if (nextButton.getAttribute("disabled") == null) {
                            try {
                                nextPageCount++;
                                if (nextPageCount > 10) {
                                    break;
                                }
                                nextButton.click();
                            } catch (ElementClickInterceptedException e) {
                                // 클릭할 수 없을 경우 스크롤을 시도하거나 재시도
                                JavascriptExecutor jsExecutor = (JavascriptExecutor) mainCrawling.getDriver();
                                jsExecutor.executeScript("arguments[0].click();", nextButton);
                            } finally {
                                log.info("[[{} thread - Next Page!!!]]", Thread.currentThread().threadId());
                                JavascriptExecutor js = (JavascriptExecutor) mainCrawling.getDriver();
                                js.executeScript("window.gc && window.gc();");
                            }
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            } catch (Exception ignored) {

            }
        }

        // driver quit
        mainCrawling.quitDriver();
    }

    public void postProcessing(Crawling curCrawling, List<String> imgUrls, Integer count) {
        // 주소
        String curAddress = curCrawling.getTextByCssSelector(addressSelector);
        if (curAddress == null) {
            return;
        }
        //TODO: 여기 서울만 되게 해놨음.
//        if (!curAddress.startsWith("서울")) {
//            return;
//        }
        String curCoordinate = getCoordinate(curAddress);
        if (curCoordinate == null) {
            return;
        }
        // 3번 이상 본 경우를 위해 추가함
        try {
            WebElement more3Element = curCrawling.getDriver().findElement(By.cssSelector(more3CloseSelector));

            more3Element.click();

            more3Element.clear();
        } catch (NoSuchElementException ignored) {

        }
        // information extraction
        // house id
        Integer houseId = 0;
        try {
//            houseId = Integer.parseInt(curCrawling.getTextByCssSelector(houseIdSelector).split(" ")[1]);
            houseId = Integer.parseInt(curCrawling.getDriver().findElement(By.cssSelector(houseIdSelector)).getText().split(" ")[1]);
        } catch (NumberFormatException e) {
            return;
        }
        // url
        String curUrl = curCrawling.getDriver().getCurrentUrl();
        // price
//        String priceType = curCrawling.getTextByCssSelector(priceTypeSelector);
        String[] priceInfo = curCrawling.getDriver().findElement(By.cssSelector(priceTypeSelector)).getText().split(" ");
        String priceType = priceInfo[0];
        Integer price = 0;
        Integer priceForWs = null;
        try {
//            String[] priceList = curCrawling.getTextByCssSelector(priceSelector).split("/");
            String[] priceList = priceInfo[1].split("/");
            price = convertToNumber(priceList[0]);
            if (priceType.equals("월세")) {
                priceForWs = convertToNumber(priceList[1]);
            }
        } catch (IllegalStateException e) {
            return;
        }
        // maintenance fee
//        String[] maintenanceFeeList = curCrawling.getTextByCssSelector(maintenanceFeeSelector).split(" ");
        String[] maintenanceFeeList = curCrawling.getDriver().findElement(By.cssSelector(maintenanceFeeSelector)).getText().split(" ");
        Integer maintenanceFee = null;
        if (maintenanceFeeList.length > 1) {
            String temp = maintenanceFeeList[1];
            try {
                maintenanceFee = convertToNumber(temp.trim().substring(0, temp.indexOf("만")));
            } catch (NumberFormatException ignored) {
            }
        }
        // basic info
        String housingType = "";
        Boolean isMultiLayer = false;
        Boolean isSeparateType = false;
        String floor = "";
        Double size = 0d;
        Integer roomNum = 0;
        Integer washroomNum = 0;
        String direction = "";
        LocalDate completionDate = LocalDate.now();
//        List<WebElement> basicInfoOuter = curCrawling.getElementListByCssSelector(basicInfoSelector);
        WebElement basicInfoOuter = curCrawling.getDriver().findElement(By.cssSelector(basicInfoSelector));
        List<WebElement> basicInfos = basicInfoOuter.findElements(By.cssSelector("li"));
        basicInfoOuter = null;
        for (WebElement element : basicInfos) {
            String elText = element.findElement(By.cssSelector("h1")).getText();
            switch (elText) {
                // housing type, is multi layer, is separate type
                case "방종류":
                    housingType = element.findElement(By.cssSelector("p")).getText();
                    String[] housingTypeList = housingType.split("\\(");
                    housingType = housingTypeList[0];
                    if (housingTypeList.length > 1) {
                        if (housingTypeList[1].contains("복층")) {
                            isMultiLayer = true;
                        }
                        if (housingTypeList[1].contains("분리형")) {
                            isSeparateType = true;
                        }
                    }
                    break;
                // floor
                case "해당층/건물층":
                    floor = element.findElement(By.cssSelector("p")).getText();
                    break;
                // size
                case "공급면적":
                case "전용면적":
                case "계약면적":
                case "전용/공급면적":
                case "전용/계약면적":
                    String sizeStr = element.findElement(By.cssSelector("p")).getText();
                    if (sizeStr.contains("/")) {
                        sizeStr = sizeStr.split("/")[1];
                    }
                    sizeStr = sizeStr.substring(0, sizeStr.length() - 1);
                    try {
                        size = Double.parseDouble(sizeStr);
                    } catch (NumberFormatException e) {
                        size = 0d;
                    }
                    break;
                // room num, washroom num
                case "방 수/욕실 수":
                    String[] roomNumList = element.findElement(By.cssSelector("p")).getText().split("/");
                    try {
                        roomNum = Integer.parseInt(roomNumList[0].substring(0, roomNumList[0].length() - 1));
                    } catch (NumberFormatException e) {
                        roomNum = 0;
                    }
                    try {
                        washroomNum = Integer.parseInt(roomNumList[1].substring(0, roomNumList[1].length() - 1));
                    } catch (NumberFormatException e) {
                        washroomNum = 0;
                    }
                    break;
                // direction
                case "방향":
                    direction = element.findElement(By.cssSelector("p")).getText();
                    break;
                // completion date
                case "사용승인일":
                    String completionDateStr = element.findElement(By.cssSelector("p")).getText();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
                    // 문자열을 LocalDate로 변환합니다.
                    completionDate = LocalDate.parse(completionDateStr, formatter);
                    break;
            }
        }
        for (WebElement el : basicInfos) {
            el = null;
        }

        // option
        StringBuilder option = new StringBuilder();
        List<WebElement> options = curCrawling.getDriver().findElements(By.cssSelector(optionSelector));
        if (!options.isEmpty()) {
            for (int i = 0; i < options.size(); i++) {
                if (i > 0) {
                    option.append(",");
                }
                option.append(options.get(i).getText());
            }
        }
        for (WebElement el : options) {
            el = null;
        }

        //
        double longitude = Double.parseDouble(curCoordinate.split("/")[0]);
        double latitude = Double.parseDouble(curCoordinate.split("/")[1]);
        House house = new House(
                houseId,
                curUrl,
                priceType,
                price,
                priceForWs,
                maintenanceFee,
                housingType,
                isMultiLayer,
                isSeparateType,
                floor,
                size,
                roomNum,
                washroomNum,
                direction,
                completionDate,
                option.toString(),
                curAddress,
                longitude,
                latitude,
                String.join("@@@", imgUrls)
        );

        House dbHouse = null;
        try {
            dbHouse = houseService.getHouse(houseId);
        } catch (DataNotFoundException ignored) {

        }
        if (dbHouse == null || dbHouse.getCreatedAt() == null) {
            house.setCreatedAt(LocalDateTime.now());
        } else {
            house.setUpdatedAt(LocalDateTime.now());
        }
        house.setCheckedAt(LocalDateTime.now());
        Point<G2D> point = new Point<>(new G2D(longitude, latitude), CoordinateReferenceSystems.WGS84);
        house.setCoordinate(point);  // 좌표 저장

        houseService.saveHouse(house);
//        System.out.println(house);

        count++;
    }

    public static int convertToNumber(String text) {
        text = text.replace(" ", ""); // 공백 제거
        int result = 0;
        int num = 0;

        // 억 단위를 처리
        if (text.contains("억")) {
            String[] parts = text.split("억");
            if (!parts[0].isEmpty()) {
                num = Integer.parseInt(parts[0]);
                result += num * 10000;
            }
            if (parts.length > 1 && !parts[1].isEmpty()) {
                num = Integer.parseInt(parts[1]);
                result += num;
            }
        } else if (!text.isEmpty()) {
            // 억 단위가 없으면 그냥 숫자를 처리
            result = Integer.parseInt(text);
        }

        return result;
    }

    public static String getCoordinate(String address) {
        StringBuilder sb = new StringBuilder("https://api.vworld.kr/req/address");
        sb.append("?service=address");
        sb.append("&request=getCoord");
        sb.append("&format=json");
        sb.append("&key=" + apiKey);
        sb.append("&type=" + searchType);
        sb.append("&refine=false");
        sb.append("&address=").append(URLEncoder.encode(address, StandardCharsets.UTF_8));

        try {
            // HttpClient 생성
            HttpClient client = HttpClient.newHttpClient();
            // HttpRequest 생성
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sb.toString()))
                    .build();
            // 요청 보내기
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // 응답 처리
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObj = jsonElement.getAsJsonObject();

            JsonObject jsrs = jsonObj.getAsJsonObject("response");
            JsonObject jsResult = jsrs.getAsJsonObject("result");
            JsonObject jspoitn = jsResult.getAsJsonObject("point");

            return jspoitn.get("x").getAsString() + "/" + jspoitn.get("y").getAsString();
        } catch (Exception e) {
            return null;
        }
    }
}