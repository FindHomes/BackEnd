package com.findhomes.findhomesbe.service;

import com.findhomes.findhomesbe.DTO.HouseDetailResponse;
import com.findhomes.findhomesbe.entity.FavoriteHouse;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.RecentlyViewedHouse;
import com.findhomes.findhomesbe.entity.User;
import com.findhomes.findhomesbe.exception.exception.DataNotFoundException;
import com.findhomes.findhomesbe.repository.FavoriteHouseRepository;
import com.findhomes.findhomesbe.repository.HouseRepository;
import com.findhomes.findhomesbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteHouseService {
    private final UserService userService;
    private final HouseService houseService;
    private final FavoriteHouseRepository favoriteHouseRepository;
    private final UserRepository userRepository;
    private final HouseRepository houseRepository;

    public List<House> getFavoriteHouses(String userId) {
        User user = userService.getUser(userId);
        // 최근 본 매물 리스트가 비어 있으면 예외 발생
        List<FavoriteHouse> favoriteHouseList = user.getFavoriteHouseList();
        if (favoriteHouseList.isEmpty()) {
            throw new DataNotFoundException("찜한 방 이 존재하지 않습니다");
        }
        // 최근에 찜한 순으로 정렬하여 House 리스트로 변환
        return favoriteHouseList.stream().sorted(Comparator.comparing(FavoriteHouse::getCreatedAt).reversed()).map(FavoriteHouse::getHouse)  // House 객체로 변환
                .collect(Collectors.toList());
    }

    // 찜한 매물 추가
    public void addFavoriteHouse(String userId, int houseId) {
        User user = userService.getUser(userId);
        House house = houseService.getHouse(houseId);
        if (favoriteHouseRepository.findByUserAndHouse(user, house).isPresent()) {
            throw new DataNotFoundException("이미 찜한 매물입니다.");
        }
        FavoriteHouse favoriteHouse = new FavoriteHouse(user, house);
        favoriteHouseRepository.save(favoriteHouse);  // save() 호출하면 JPA가 관계 엔티티도 자동 업데이트
    }

    // 찜한 매물 삭제
    public void removeFavoriteHouse(String userId, int houseId) {
        User user = userService.getUser(userId);
        House house = houseService.getHouse(houseId);
        // delete() 호출로 찜한 매물 삭제
        favoriteHouseRepository.findByUserAndHouse(user, house).ifPresentOrElse(favoriteHouseRepository::delete, () -> {
            throw new DataNotFoundException("유저와 매물 간 찜하기 데이터가 없습니다.");
        });
    }
    // 찜한 매물인지 확인
    public boolean isFavoriteHouse(String userId, int houseId) {
        User user = userService.getUser(userId);  // User 객체 가져오기
        House house = houseService.getHouse(houseId);  // House 객체 가져오기
        return favoriteHouseRepository.findByUserAndHouse(user, house).isPresent();  // 찜 여부 확인
    }

    public HouseDetailResponse getHouseDetailwithFavoriteFlag(String userId, int houseId) {
        House house = houseRepository.findById(houseId)
                .orElseThrow(() -> new DataNotFoundException("해당 매물을 찾을 수 없습니다."));

        boolean isFavorite = isFavoriteHouse(userId, houseId);

        return new HouseDetailResponse(house, isFavorite, true, 200, "매물 조회 성공");
    }
}

