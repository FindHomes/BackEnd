package com.findhomes.findhomesbe.crawling;

import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public abstract class CrawlingConst {
    public static final int MAX_WAIT_TIME = 10;
    // 지오코더 API
    public static final String apiKey = "F6A7710C-3505-3390-8FE5-25CAB7F0001A";
    public static final String searchType = "parcel";
    // css selector
    public static final String nextBtnSelector = ".izoyfh button";
    public static final String saleElSelector = ".kywSSM";
    public static final String more3CloseSelector = ".jKiLYt";
    public static final String houseIdSelector = ".dZqSOl";
    public static final String priceTypeSelector = ".haYYrw";
    public static final String priceSelector = ".bQSaMO";
    public static final String maintenanceFeeSelector = ".cVraOf p";
    public static final String basicInfoSelector = "ul.kQzujR";
    public static final String basicInfoNameSelector = ".jJKkgc";
    public static final String basicInfoValueSelector = ".iMduqg";
    public static final String optionSelector = ".fZeaKW";
    public static final String addressSelector = ".hBlCFR";

    public static List<String> oneTwoUrls = new ArrayList<>();
    public static List<String> aptUrls = new ArrayList<>();
    public static List<String> houseUrls = new ArrayList<>();
    public static List<String> officeUrls = new ArrayList<>();

    private static <T> List<T> getShuffledList(List<T> list) {
        List<T> shuffledList = new ArrayList<>(list); // 리스트 복사
        Collections.shuffle(shuffledList);            // 복사한 리스트를 섞음
        return shuffledList;                          // 섞인 리스트 반환
    }

    public static Supplier<List<String>> shuffledOneTwoUrls = () -> getShuffledList(oneTwoUrls);
    public static Supplier<List<String>> shuffledAptUrls = () -> getShuffledList(aptUrls);
    public static Supplier<List<String>> shuffledHouseUrls = () -> getShuffledList(houseUrls);
    public static Supplier<List<String>> shuffledOfficeUrls = () -> getShuffledList(officeUrls);


    // url 생성
    static {
        // 위도, 경도 범위
        double minLat = 33.1182118;
        double maxLat = 38.604828;
        double minLng = 125.8501747;
        double maxLng = 129.5570305;

        // 위도와 경도의 증가량
        double latStep = 0.035;
        double lngStep = 0.057;

        // 위도와 경도에 따라 URL을 생성하여 리스트에 추가
        for (double lat = minLat; lat <= maxLat; lat += latStep) {
            for (double lng = minLng; lng <= maxLng; lng += lngStep) {
                // onetwo URL 생성 및 추가
                oneTwoUrls.add(String.format("https://www.dabangapp.com/map/onetwo?m_lat=%.7f&m_lng=%.7f&m_zoom=14", lat, lng));

                // apt URL 생성 및 추가
                aptUrls.add(String.format("https://www.dabangapp.com/map/apt?m_lat=%.7f&m_lng=%.7f&m_zoom=14", lat, lng));

                // house URL 생성 및 추가
                houseUrls.add(String.format("https://www.dabangapp.com/map/house?m_lat=%.7f&m_lng=%.7f&m_zoom=14", lat, lng));

                // officetel URL 생성 및 추가
                officeUrls.add(String.format("https://www.dabangapp.com/map/officetel?m_lat=%.7f&m_lng=%.7f&m_zoom=14", lat, lng));
            }
        }
    }
}
