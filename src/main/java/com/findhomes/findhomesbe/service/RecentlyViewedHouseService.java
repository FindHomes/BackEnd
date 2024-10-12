package com.findhomes.findhomesbe.service;

import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.RecentlyViewedHouse;
import com.findhomes.findhomesbe.entity.User;
import com.findhomes.findhomesbe.repository.RecentlyViewedHouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class RecentlyViewedHouseService {

    private final RecentlyViewedHouseRepository recentlyViewedHouseRepository;
    private final HouseService houseService;
    private final UserService userService;

    public void saveOrUpdateRecentlyViewedHouse(String userId, Integer houseId) {
        User user = userService.getUser(userId);
        House house = houseService.getHouse(houseId);

        // 이미 존재하는지 확인
        Optional<RecentlyViewedHouse> existingRecord = recentlyViewedHouseRepository.findByUserAndHouse(user, house);

        if (existingRecord.isPresent()) {
            // 기존 기록이 있으면 viewedAt만 업데이트
            RecentlyViewedHouse recentlyViewedHouse = existingRecord.get();
            recentlyViewedHouse.updateViewedAt();
            recentlyViewedHouseRepository.save(recentlyViewedHouse);  // 업데이트 후 저장
        } else {
            // 기록이 없으면 새로 추가
            RecentlyViewedHouse recentlyViewedHouse = new RecentlyViewedHouse(user, house);
            recentlyViewedHouseRepository.save(recentlyViewedHouse);
        }
    }
}
