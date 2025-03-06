package com.findhomes.findhomesbe.domain.house;

import com.findhomes.findhomesbe.domain.condition.domain.SessionKeys;
import com.findhomes.findhomesbe.domain.condition.domain.AllConditions;
import com.findhomes.findhomesbe.domain.condition.domain.HouseWithCondition;
import com.findhomes.findhomesbe.domain.condition.service.HouseWithConditionService;
import com.findhomes.findhomesbe.domain.house.dto.HouseDetailResponse;
import com.findhomes.findhomesbe.domain.house.dto.HouseDetailSearchResponse;
import com.findhomes.findhomesbe.domain.house.dto.ResponseHouse;
import com.findhomes.findhomesbe.domain.house.dto.StatisticsResponse;
import com.findhomes.findhomesbe.domain.house.service.FavoriteHouseService;
import com.findhomes.findhomesbe.domain.house.service.RecentlyViewedHouseService;
import com.findhomes.findhomesbe.global.Response;
import com.findhomes.findhomesbe.global.exception.exception.ClientIllegalArgumentException;
import com.findhomes.findhomesbe.global.auth.SecurityService;
import com.findhomes.findhomesbe.global.exception.exception.PreconditionRequiredException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class HouseController {
    private final HouseWithConditionService houseWithConditionService;
    private final SecurityService securityService;
    private final FavoriteHouseService favoriteHouseService;
    private final RecentlyViewedHouseService recentlyViewedHouseService;

    @GetMapping("/api/search/statistics")
    @Operation(summary = "통계 정보 가져오기", description = "현재 결과에 반영된 데이터 정보를 가져옵니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "매물 응답 완료", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = StatisticsResponse.class))}), @ApiResponse(responseCode = "401", description = "세션이 유효하지 않습니다", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Response.class))}), @ApiResponse(responseCode = "428", description = "세션에 필수 데이터가 없습니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Response.class))})})
    public ResponseEntity<StatisticsResponse> getStatistics(@Parameter(hidden = true) @SessionAttribute(value = SessionKeys.HOUSE_RESULTS_KEY, required = false) List<HouseWithCondition> houseWithConditions, @Parameter(hidden = true) @SessionAttribute(value = SessionKeys.ALL_CONDITIONS, required = false) AllConditions allConditions) {
        return new ResponseEntity<>(StatisticsResponse.of(houseWithConditions, allConditions, true, 200, "응답 성공"), HttpStatus.OK);
    }

    // 최근 본 매물 조회 API
    @GetMapping("/api/houses/recently-viewed")
    @Operation(summary = "최근 본 매물", description = "사용자가 최근에 본 매물을 최신순으로 반환합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "성공적으로 최근 본 매물을 반환함"), @ApiResponse(responseCode = "401", description = "인증 오류"), @ApiResponse(responseCode = "404", description = "최근 본 매물이 없습니다")})
    public ResponseEntity<Response> getRecentlyViewedHouses(HttpServletRequest httpRequest) {
        String userId = securityService.getUserId(httpRequest);
        List<ResponseHouse> recentlyViewedHouses = recentlyViewedHouseService.getRecentlyViewedHouses(userId).stream().map(house -> new ResponseHouse(house, true)).collect(Collectors.toList());
        ;
        // 최근 본 매물이 없는 경우, 빈 리스트 반환
        if (recentlyViewedHouses.isEmpty()) {
            return new ResponseEntity<>(new Response(true, 200, "최근 본 매물이 없습니다", Collections.emptyList()), HttpStatus.OK);
        }
        return new ResponseEntity<>(new Response(true, 200, "최근 본 매물을 불러오는데 성공하였습니다", recentlyViewedHouses), HttpStatus.OK);
    }

    // 찜한 방 매물 조회 API
    @GetMapping("/api/houses/favorite")
    @Operation(summary = "찜한 방 ", description = "사용자가 찜한 매물을 반환합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "성공적으로 찜한 매물을 반환함"), @ApiResponse(responseCode = "401", description = "인증 오류"), @ApiResponse(responseCode = "404", description = "찜한 방이 없습니다")})
    public ResponseEntity<Response> getfavoriteHouses(HttpServletRequest httpRequest) {
        String userId = securityService.getUserId(httpRequest);
        List<ResponseHouse> favoriteHouses = favoriteHouseService.getFavoriteHouses(userId).stream().map(house -> new ResponseHouse(house, true)).collect(Collectors.toList());
        // 찜한 방이 없는 경우, 빈 리스트 반환
        if (favoriteHouses.isEmpty()) {
            return new ResponseEntity<>(new Response(true, 200, "찜한 방이 없습니다", Collections.emptyList()), HttpStatus.OK);
        }
        return new ResponseEntity<>(new Response(true, 200, "찜한 매물을 불러오는데 성공하였습니다", favoriteHouses), HttpStatus.OK);
    }

    // 찜하기 API
    @PostMapping("/api/houses/{houseId}/favorite")
    @Operation(summary = "찜하기", description = "찜하기 버튼을 눌러 찜을 등록하거나 해제합니다. action 파라미터로는 add 또는 remove 값으로 찜 등록 및 해제를 구분합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "찜하기 처리 완료"), @ApiResponse(responseCode = "404", description = "입력 id에 해당하는 매물이 없습니다")})
    public ResponseEntity<HouseDetailResponse> manageFavoriteOnHouse(HttpServletRequest httpRequest, @PathVariable("houseId") int houseId, @RequestParam("action") String action) {
        String userId = securityService.getUserId(httpRequest);
        if (action.equalsIgnoreCase("add")) {
            favoriteHouseService.addFavoriteHouse(userId, houseId);
        } else if (action.equalsIgnoreCase("remove")) {
            favoriteHouseService.removeFavoriteHouse(userId, houseId);
        } else {
            throw new ClientIllegalArgumentException("잘못된 action 값입니다. add 또는 remove를 사용하세요.");
        }
        return new ResponseEntity<>(new HouseDetailResponse(true, 200, "찜하기 처리 완료"), HttpStatus.OK);

    }

    // 매물 상세페이지 API
    @GetMapping("/api/houses/{houseId}")
    @Operation(summary = "매물 상세페이지", description = "매물을 클릭하고 상세페이지로 이동합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "매물 응답 완료"), @ApiResponse(responseCode = "401", description = "유효한 session이 없습니다. 필수 조건 입력 창으로 돌아가야 합니다."), @ApiResponse(responseCode = "404", description = "입력 id에 해당하는 매물이 없습니다")})
    public ResponseEntity<HouseDetailResponse> getHouseDetail(HttpServletRequest httpRequest, @PathVariable int houseId) {
        String userId = securityService.getUserId(httpRequest);
        // 최근 본 방에 추가
        recentlyViewedHouseService.saveOrUpdateRecentlyViewedHouse(userId, houseId);
        HouseDetailResponse houseDetailResponse = favoriteHouseService.getHouseDetailwithFavoriteFlag(userId, houseId);
        return new ResponseEntity<>(houseDetailResponse, HttpStatus.OK);
    }


    @GetMapping("/api/search/houses/{houseId}")
    @Operation(summary = "매물 추천 결과에서 매물 상세 화면", description = "매물 추천 결과 화면에서 매물 상세 정보 화면으로 들어갈 때, 매물의 상세 정보를 받아오는 api입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매물 응답 완료", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = HouseDetailSearchResponse.class))}),
            @ApiResponse(responseCode = "404", description = "결과 리스트에 해당 houseId를 가진 매물이 없습니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "428", description = "세션에 필수 데이터가 없습니다. 처음 화면으로 돌아가야 합니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Response.class))})
    })
    public ResponseEntity<HouseDetailSearchResponse> getHouseDetailSearch(
            @Parameter(hidden = true) @SessionAttribute(value = SessionKeys.HOUSE_RESULTS_KEY, required = false) List<HouseWithCondition> houseWithConditions,
            @PathVariable int houseId
    ) {
        if (houseWithConditions == null) {
            throw new PreconditionRequiredException("매물 탐색 데이터가 세션에 없습니다.");
        }
        HouseWithCondition result = houseWithConditionService.findByHouseId(houseWithConditions, houseId);

        return new ResponseEntity<>(new HouseDetailSearchResponse(result, true, 200, "응답 성공"), HttpStatus.OK);
    }
}
