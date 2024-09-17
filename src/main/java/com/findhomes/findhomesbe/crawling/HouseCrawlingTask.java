package com.findhomes.findhomesbe.crawling;

import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.repository.HouseRepository;
import com.findhomes.findhomesbe.service.HouseService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import org.openqa.selenium.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class HouseCrawlingTask {
    private final HouseService houseService;

    private static List<House> results = new ArrayList<>();
    private static final int MAX_WAIT_TIME = 10;
    // 지오코더 API
    private static final String apiKey = "F6A7710C-3505-3390-8FE5-25CAB7F0001A";
    private static final String searchType = "parcel";
    private static int count = 0;
    // css selector
    private final String nextBtnSelector = ".izoyfh button";
    private final String saleElSelector = ".kywSSM";
    private final String more3CloseSelector = ".jKiLYt";
    private final String houseIdSelector = ".dZqSOl";
    private final String priceTypeSelector = ".haYYrw";
    private final String priceSelector = ".bQSaMO";
    private final String maintenanceFeeSelector = ".cVraOf p";
    private final String basicInfoSelector = "ul.kQzujR";
    private final String basicInfoNameSelector = ".jJKkgc";
    private final String basicInfoValueSelector = ".iMduqg";
    private final String optionSelector = ".fZeaKW";
    private final String addressSelector = ".hBlCFR";

    // 페이지 url
    List<String> urls = List.of(
            "https://www.dabangapp.com/map/onetwo?m_lat=37.7154634&m_lng=126.6532925&m_zoom=14",
            "https://www.dabangapp.com/map/onetwo?m_lat=37.7153616&m_lng=126.7550877&m_zoom=12",
            "https://www.dabangapp.com/map/onetwo?m_lat=37.6865676&m_lng=127.0232228&m_zoom=12",
            "https://www.dabangapp.com/map/onetwo?m_lat=37.6805901&m_lng=127.3442295&m_zoom=12",
            "https://www.dabangapp.com/map/onetwo?m_lat=37.4316898&m_lng=129.1852978&m_zoom=11",
            "https://www.dabangapp.com/map/onetwo?m_lat=37.5861767&m_lng=126.7730263&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=37.5854965&m_lng=126.901944&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=37.583456&m_lng=127.0258835&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=37.5814154&m_lng=127.1820954&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=37.5740689&m_lng=127.3357323&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=37.511186&m_lng=126.5716675&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=37.5020622&m_lng=126.728566&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=37.4932097&m_lng=126.8882111&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=37.4853097&m_lng=127.0291451&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=37.4735943&m_lng=127.2054413&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=37.3435119&m_lng=127.9699353&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=37.2671164&m_lng=127.5571755&m_zoom=12",
            "https://www.dabangapp.com/map/onetwo?m_lat=36.9965168&m_lng=127.0764807&m_zoom=14",
            "https://www.dabangapp.com/map/onetwo?m_lat=36.9972023&m_lng=127.1672896&m_zoom=14",
            "https://www.dabangapp.com/map/onetwo?m_lat=36.8418469&m_lng=127.142227&m_zoom=14",
            "https://www.dabangapp.com/map/onetwo?m_lat=36.7986976&m_lng=127.1445444&m_zoom=14",
            "https://www.dabangapp.com/map/onetwo?m_lat=36.7887656&m_lng=126.9963578&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=36.6961204&m_lng=126.5980175&m_zoom=11",
            "https://www.dabangapp.com/map/onetwo?m_lat=36.3163317&m_lng=127.3045758&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=36.3177149&m_lng=127.4719456&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=36.0781318&m_lng=126.9256333&m_zoom=11",
            "https://www.dabangapp.com/map/onetwo?m_lat=35.8588272&m_lng=127.1293095&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=35.7871453&m_lng=127.1387509&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=35.1471048&m_lng=126.7688206&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=35.1631046&m_lng=126.9161061&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=35.1998643&m_lng=126.9119862&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=34.5720582&m_lng=126.6990403&m_zoom=10",
            "https://www.dabangapp.com/map/onetwo?m_lat=33.3051702&m_lng=126.5314988&m_zoom=10",
            "https://www.dabangapp.com/map/onetwo?m_lat=35.2345743&m_lng=128.1564454&m_zoom=11",
            "https://www.dabangapp.com/map/onetwo?m_lat=35.2554633&m_lng=128.6039666&m_zoom=12",
            "https://www.dabangapp.com/map/onetwo?m_lat=35.2367476&m_lng=128.9180211&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=35.0732401&m_lng=129.028056&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=35.173138&m_lng=129.0568093&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=35.2111555&m_lng=129.1783455&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=35.3670369&m_lng=129.1296795&m_zoom=12",
            "https://www.dabangapp.com/map/onetwo?m_lat=35.5455274&m_lng=129.2603997&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=35.5772269&m_lng=129.4215897&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=36.0149107&m_lng=129.3688038&m_zoom=12",
            "https://www.dabangapp.com/map/onetwo?m_lat=35.8739904&m_lng=128.9039448&m_zoom=12",
            "https://www.dabangapp.com/map/onetwo?m_lat=35.8820577&m_lng=128.5547856&m_zoom=12",
            "https://www.dabangapp.com/map/onetwo?m_lat=35.9445514&m_lng=128.5407952&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=36.1356875&m_lng=128.123658&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=36.1469163&m_lng=128.3838967&m_zoom=13",
            "https://www.dabangapp.com/map/onetwo?m_lat=36.5620103&m_lng=128.7190655&m_zoom=11",
            "https://www.dabangapp.com/map/onetwo?m_lat=38.3612735&m_lng=128.6249951&m_zoom=10",
            "https://www.dabangapp.com/map/onetwo?m_lat=37.5872649&m_lng=128.8625744&m_zoom=10",
            "https://www.dabangapp.com/map/onetwo?m_lat=37.3598869&m_lng=127.967017&m_zoom=12"

    );

    public void exec(Integer startIndex) throws InterruptedException {
        Crawling mainCrawling = new Crawling()
                .setDriverWithShowing()
                .setWaitTime(MAX_WAIT_TIME);
        mainCrawling.getDriver().manage().window().maximize();
        for (int i = startIndex; i < urls.size(); i++) {
            String url = urls.get(i);
            Thread currentThread = Thread.currentThread();
            log.info("{}쓰레드 - {}번 인덱스 시작!!!!!!", currentThread.threadId(), i);
            mainCrawling.openUrl(url);

            while (true) {
                List<WebElement> salesElementList = mainCrawling.getElementListByCssSelector(saleElSelector);
                if (salesElementList == null) {
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
                        postProcessing(mainCrawling, img_urls);
                    } catch (Exception ignored) {

                    }
                }

                // 다음 버튼
                List<WebElement> nextButtonList = mainCrawling.getElementListByCssSelector(nextBtnSelector);
                if (nextButtonList != null) {
                    WebElement nextButton = nextButtonList.get(nextButtonList.size() - 1);
                    if (nextButton.getAttribute("disabled") == null) {
                        try {
                            nextButton.click();
                        } catch (ElementClickInterceptedException e) {
                            // 클릭할 수 없을 경우 스크롤을 시도하거나 재시도
                            JavascriptExecutor jsExecutor = (JavascriptExecutor) mainCrawling.getDriver();
                            jsExecutor.executeScript("arguments[0].click();", nextButton);
                        } finally {
                            System.out.println("다음 페이지!!!!!");
                        }
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        // driver quit
        mainCrawling.quitDriver();
    }

    public void postProcessing(Crawling curCrawling, List<String> imgUrls) {
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

        // img url
//        WebElement firstImgElement = curCrawling.getElementByCssSelector(".styled__Photo-sc-173484h-2");
//        if (firstImgElement != null) {
//
//        }

        //
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
                Double.parseDouble(curCoordinate.split("/")[0]),
                Double.parseDouble(curCoordinate.split("/")[1]),
                String.join("@@@", imgUrls)
        );

        count++;

        if (count % 100 == 0) {
            log.info("매물 {}개 크롤링 완료", count);
        }

        houseService.saveHouse(house);
        log.info("새로운 매물: {}", house);
//        System.out.println(++count);
//        System.out.println(house);
//        results.add(house);
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
