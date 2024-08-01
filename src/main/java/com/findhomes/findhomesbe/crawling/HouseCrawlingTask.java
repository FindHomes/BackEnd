package com.findhomes.findhomesbe.crawling;

import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.repository.HouseRepository;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
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
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class HouseCrawlingTask {
    private final HouseRepository houseRepository;

    private static List<House> results = new ArrayList<>();
    private static final int MAX_WAIT_TIME = 10;
    // 지오코더 API
    private static final String apiKey = "F6A7710C-3505-3390-8FE5-25CAB7F0001A";
    private static final String searchType = "parcel";
    private static int count = 0;
    // css selector
    private final String nextBtnSelector = ".izoyfh button";
    private final String saleElSelector = ".qaJAh";
    private final String more3CloseSelector = ".kkqmBQ";
    private final String houseIdSelector = ".izbaWE";
    private final String priceTypeSelector = ".gfmTEV";
    private final String priceSelector = ".bQSaMO";
    private final String maintenanceFeeSelector = ".SFyWA p";
    private final String basicInfoSelector = "ul.cEOUfe";
    private final String basicInfoNameSelector = ".jJKkgc";
    private final String basicInfoValueSelector = ".iMduqg";
    private final String optionSelector = ".fZeaKW";
    private final String addressSelector = ".lKcXv";

    // 페이지 url
    List<String> urls = List.of(
            "https://www.dabangapp.com/map/house?m_lat=37.3142326&m_lng=127.0345524&m_zoom=12"
    );

    public void exec() {
        Crawling mainCrawling = new Crawling()
                .setDriverWithShowing()
                .setWaitTime(MAX_WAIT_TIME);
        //mainCrawling.getDriver().manage().window().maximize();

        for (String url : urls) {
            mainCrawling.openUrl(url);

            while (true) {
                List<WebElement> salesElementList = mainCrawling.getElementListByCssSelector(saleElSelector);
                if (salesElementList == null) {
                    break;
                }
                for (WebElement element : salesElementList) {
                    // 클릭
                    element.click();

//                    try {
//                        File screenshot = ((TakesScreenshot) mainCrawling.getDriver()).getScreenshotAs(OutputType.FILE);
//                        FileUtils.copyFile(screenshot, new File("screenshot.png"));
//                    } catch (IOException e) {
//
//                    }

                    // 새로운 탭으로 전환
                    ArrayList<String> tabs = new ArrayList<>(mainCrawling.getDriver().getWindowHandles());
                    mainCrawling.getDriver().switchTo().window(tabs.get(1));

//                    try {
//                        File screenshot = ((TakesScreenshot) mainCrawling.getDriver()).getScreenshotAs(OutputType.FILE);
//                        FileUtils.copyFile(screenshot, new File("screenshot2.png"));
//                    } catch (IOException e) {
//
//                    }

                    try {
                        postProcessing(mainCrawling);
                    } catch (Exception ignored) {

                    }

                    mainCrawling.getDriver().close();

                    // 원래 탭으로 전환
                    mainCrawling.getDriver().switchTo().window(tabs.get(0));
                }

                // 다음 버튼
                List<WebElement> nextButtonList = mainCrawling.getElementListByCssSelector(nextBtnSelector);
                if (nextButtonList != null) {
                    WebElement nextButton = nextButtonList.get(nextButtonList.size() - 1);
                    if (nextButton.getAttribute("disabled") == null) {
                        nextButton.click();
                        System.out.println("다음 페이지!!!!!");
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

    public void postProcessing(Crawling curCrawling) {
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
        String priceType = curCrawling.getDriver().findElement(By.cssSelector(priceTypeSelector)).getText();
        Integer price = 0;
        Integer priceForWs = null;
        try {
//            String[] priceList = curCrawling.getTextByCssSelector(priceSelector).split("/");
            String[] priceList = curCrawling.getDriver().findElement(By.cssSelector(priceSelector)).getText().split("/");
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
        List<WebElement> basicInfoOuter = curCrawling.getDriver().findElements(By.cssSelector(basicInfoSelector));
        List<WebElement> basicInfos = basicInfoOuter.get(1).findElements(By.cssSelector("li"));
        for (WebElement element : basicInfos) {
            String elText = element.findElement(By.cssSelector(basicInfoNameSelector)).getText();
            switch (elText) {
                // housing type, is multi layer, is separate type
                case "방종류":
                    housingType = element.findElement(By.cssSelector(basicInfoValueSelector + " p")).getText();
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
                    floor = element.findElement(By.cssSelector(basicInfoValueSelector)).getText();
                    break;
                // size
                case "공급면적":
                case "전용면적":
                case "계약면적":
                case "전용/공급면적":
                case "전용/계약면적":
                    String sizeStr = element.findElement(By.cssSelector(basicInfoValueSelector + " p")).getText();
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
                    String[] roomNumList = element.findElement(By.cssSelector(basicInfoValueSelector)).getText().split("/");
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
                    direction = element.findElement(By.cssSelector(basicInfoValueSelector)).getText();
                    if (direction.contains("거실")) {
                        direction = direction.substring(0, direction.indexOf("거"));
                    } else if (direction.contains("안방")) {
                        direction = direction.substring(0, direction.indexOf("안"));
                    }
                    break;
                // completion date
                case "사용승인일":
                    String completionDateStr = element.findElement(By.cssSelector(basicInfoValueSelector)).getText();
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
                Double.parseDouble(curCoordinate.split("/")[1])
        );

        count++;

        if (count % 100 == 0) {
            log.info("매물 {}개 크롤링 완료", count);
        }

        houseRepository.save(house);
//        System.out.println(++count);
//        System.out.println(house);
//        results.add(house);
    }

    public static int printCountAndHouse(String threadName, String house) {
        System.out.println(++count);
        if (count % 100 == 0) {
            System.out.println(threadName + " / " + house);
            return count;
        }
        System.out.println(threadName + " / " + house);
        return -1;
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
