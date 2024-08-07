package com.findhomes.findhomesbe.controller;

import com.findhomes.findhomesbe.crawling.IndustryCrawlingTask;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.crawling.HouseCrawlingTask;
import com.findhomes.findhomesbe.repository.HouseRepository;
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

    @GetMapping("/api/crawling")
    public ResponseEntity<Void> crawlingHouse() {
        houseCrawlingTask.exec();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/api/crawling/restaurant")
    public ResponseEntity<Void> crawlingRestaurant(@RequestParam Integer start, @RequestParam Integer end) {
        industryCrawlingTask.exec(start, end);
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
                .x(127.0)
                .y(37.4)
                .build();
        houseRepository.save(newHouse);
        System.out.println(newHouse.getHouseId());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
