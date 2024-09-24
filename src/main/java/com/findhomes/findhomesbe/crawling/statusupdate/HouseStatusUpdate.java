package com.findhomes.findhomesbe.crawling.statusupdate;

import com.findhomes.findhomesbe.crawling.Crawling;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.repository.HouseRepository;
import com.findhomes.findhomesbe.service.HouseService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class HouseStatusUpdate {
//    private static final int pageSize = 500;
//    private final HouseRepository houseRepository;
//
//    @PostConstruct
//    public void startStatusUpdate() {
//        //statusUpdate(); // 비동기 작업 ㅇㅇㅇㅇ
//    }
//
//    @Async
//    public void statusUpdate() {
//        PageRequest pageRequest = PageRequest.of(0, pageSize);
//        Page<House> housesPage = houseRepository.findAll(pageRequest);
//        int pageCount = housesPage.getTotalPages();
//
//        while (true) {
//            for (int i = 0; i < pageCount; i++) {
//                Crawling crawling = new Crawling().setDriver(true, false).setWaitTime(5);
//                PageRequest req = PageRequest.of(i, pageSize);
//                Page<House> houses = houseRepository.findAll(req);
//                int count = 0;
//                for (House house : houses) {
//                    count++;
//                    if (count % 10 == 0) {
//                        crawling.quitDriver();
//                        crawling = new Crawling().setDriver(true, false).setWaitTime(5);
//                        crawling.openUrl(house.getUrl());
//                    } else {
//                        crawling.openUrl(house.getUrl());
//                    }
//
//                    WebElement element = crawling.getElementByCssSelector("#container-room-root");
//                    if (element == null) {
//                        log.info("id: {}\tstatus: {}\turl: {}", house.getHouseId(), "DELETED", house.getUrl());
//                    } else {
//                        log.info("id: {}\tstatus: {}\turl: {}", house.getHouseId(), "ACTIVE", house.getUrl());
//                    }
//                }
//            }
//        }
//    }
}
