package com.findhomes.findhomesbe.controller;

import com.findhomes.findhomesbe.DTO.HouseDetailSearchResponse;
import com.findhomes.findhomesbe.DTO.Response;
import com.findhomes.findhomesbe.condition.domain.HouseWithCondition;
import com.findhomes.findhomesbe.condition.service.HouseWithConditionService;
import com.findhomes.findhomesbe.exception.exception.PreconditionRequiredException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class RecommendController {

    private final HouseWithConditionService houseWithConditionService;

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
