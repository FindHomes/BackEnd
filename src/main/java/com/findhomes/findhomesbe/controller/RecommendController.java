package com.findhomes.findhomesbe.controller;

import com.findhomes.findhomesbe.DTO.HouseDetailResponse;
import com.findhomes.findhomesbe.DTO.HouseDetailSearchResponse;
import com.findhomes.findhomesbe.DTO.ManConRequest;
import com.findhomes.findhomesbe.condition.domain.HouseWithCondition;
import com.findhomes.findhomesbe.exception.exception.DataNotFoundException;
import com.findhomes.findhomesbe.login.SecurityService;
import com.findhomes.findhomesbe.service.FavoriteHouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;
import java.util.Optional;

import static com.findhomes.findhomesbe.controller.MainController.HOUSE_RESULTS_KEY;

@RestController
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class RecommendController {

    private final SecurityService securityService;
    private final FavoriteHouseService favoriteHouseService;

    @GetMapping("/api/search/houses/{houseId}")
    @Operation(summary = "매물 추천 결과에서 매물 상세 화면", description = "매물 추천 결과 화면에서 매물 상세 정보 화면으로 들어갈 때, 매물의 상세 정보를 받아오는 api입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매물 응답 완료"),
            @ApiResponse(responseCode = "428", description = "세션에 필수 데이터가 없습니다. 처음 화면으로 돌아가야 합니다.")
    })
    public ResponseEntity<HouseDetailSearchResponse> getHouseDetailSearch(
            @PathVariable int houseId,
            @SessionAttribute(value = HOUSE_RESULTS_KEY, required = false) List<HouseWithCondition> houseWithConditions
    ) {
        HouseWithCondition houseWithCondition = houseWithConditions.stream()
                .filter(hwc -> hwc.getHouse().getHouseId().equals(houseId))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("해당 매물이 추천 결과에 없습니다."));

        return new ResponseEntity<>(new HouseDetailSearchResponse(houseWithCondition, true, 200, "응답 성공"), HttpStatus.OK);
    }
}
