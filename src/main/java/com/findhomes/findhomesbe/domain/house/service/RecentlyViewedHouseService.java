package com.findhomes.findhomesbe.domain.house.service;

import com.findhomes.findhomesbe.domain.house.domain.House;
import com.findhomes.findhomesbe.domain.house.domain.RecentlyViewedHouse;
import com.findhomes.findhomesbe.domain.house.repository.HouseRepository;
import com.findhomes.findhomesbe.domain.house.repository.RecentlyViewedHouseRepository;
import com.findhomes.findhomesbe.domain.user.User;
import com.findhomes.findhomesbe.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RecentlyViewedHouseService {

    private final RecentlyViewedHouseRepository recentlyViewedHouseRepository;
    private final HouseService houseService;
    private final UserService userService;
    private final HouseRepository houseRepository;
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

    public List<House> getRecentlyViewedHouses(String userId) {
        User user = userService.getUser(userId);
        List<RecentlyViewedHouse> recentlyViewedHouseList = user.getRecentlyViewedHouseList();
        // 최신순으로 정렬하여 House 리스트로 변환
        return recentlyViewedHouseList.stream()
                .sorted(Comparator.comparing(RecentlyViewedHouse::getViewAt).reversed())  // 최신순 정렬
                .map(RecentlyViewedHouse::getHouse)  // House 객체로 변환
                .collect(Collectors.toList());
    }
}
