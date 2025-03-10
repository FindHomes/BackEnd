package com.findhomes.findhomesbe.domain.crawling;

import com.findhomes.findhomesbe.domain.house.domain.House;
import com.findhomes.findhomesbe.domain.house.repository.HouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class CrawlingController {
    private final HouseCrawlingTask houseCrawlingTask;
    private final IndustryCrawlingTask industryCrawlingTask;
    private final HouseRepository houseRepository;

//    // 애플리케이션 시작 후 자동으로 실행되는 메서드
//    @PostConstruct
//    public void init() throws InterruptedException {
//        houseCrawlingTask.exec(); // 크롤링 작업 자동 실행
//    }

    @GetMapping("/api/crawling")
    public ResponseEntity<Void> crawlingHouse(
//            @RequestParam(required = false, defaultValue = "0") Integer startIndex
    ) throws InterruptedException {
        houseCrawlingTask.exec();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/api/crawling/restaurant")
    public ResponseEntity<Void> crawlingRestaurant(@RequestParam String start) throws InterruptedException {
        try {
            industryCrawlingTask.exec(start);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/api/save-test")
    public ResponseEntity<Void> saveTest() {
        House newHouse = House.builder()
                .houseId(12345678)
                .url("https://kustaurant.com")
                .priceType("mm")
                .price(20000)
                .priceForWs(0)
                .housingType("원룸")
                .isMultiLayer(false)
                .isSeparateType(false)
                .floor("3층")
                .size(40d)
                .roomNum(1)
                .washroomNum(1)
                .direction("남동")
                .completionDate(LocalDate.now())
                .houseOption("에어컨")
                .address("경기도 안양시 동안구")
                .longitude(127.0)
                .latitude(37.4)
                .build();
        houseRepository.save(newHouse);
        System.out.println(newHouse.getHouseId());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
