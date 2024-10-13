package com.findhomes.findhomesbe.searchlog;

import com.findhomes.findhomesbe.DTO.HouseDetailSearchResponse;
import com.findhomes.findhomesbe.DTO.Response;
import com.findhomes.findhomesbe.DTO.SearchResponse;
import com.findhomes.findhomesbe.condition.domain.AllConditions;
import com.findhomes.findhomesbe.condition.domain.HouseWithCondition;
import com.findhomes.findhomesbe.condition.service.ConditionService;
import com.findhomes.findhomesbe.login.SecurityService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.findhomes.findhomesbe.controller.MainController.ALL_CONDITIONS;

@RestController
@RequiredArgsConstructor
public class SearchLogController {

    private final SecurityService securityService;
    private final SearchLogService searchLogService;
    private final ConditionService conditionService;

    @PostMapping("/api/search-logs")
    @Operation(summary = "현재 추천 결과의 검색 조건을 저장하는 api입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매물 응답 완료"),
            @ApiResponse(responseCode = "428", description = "세션에 필수 데이터가 없습니다. 처음 화면으로 돌아가야 합니다.")
    })
    public ResponseEntity<Response> addSearchLog(
            @Parameter(hidden = true) @SessionAttribute(value = ALL_CONDITIONS, required = false) AllConditions allConditions,
            HttpServletRequest httpRequest
    ) {
        String userId = securityService.getUserId(httpRequest);
        searchLogService.addSearchLog(allConditions, userId);

        return new ResponseEntity<>(new Response(true, 200, "추가 성공"), HttpStatus.OK);
    }

    @DeleteMapping("/api/search-logs/{searchLogId}")
    @Operation(summary = "저장된 검색 조건을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매물 응답 완료"),
            @ApiResponse(responseCode = "400", description = "path variable이 옳지 않은 경우")
    })
    public ResponseEntity<Response> deleteSearchLog(
            @PathVariable int searchLogId
    ) {
        searchLogService.deleteSearchLog(searchLogId);

        return new ResponseEntity<>(new Response(true, 200, "삭제 성공"), HttpStatus.OK);
    }

    @GetMapping("/api/search-logs")
    @Operation(summary = "저장한 검색 기록들을 불러옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매물 응답 완료", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = SearchLogResponse.class))}),
            @ApiResponse(responseCode = "428", description = "세션에 필수 데이터가 없습니다. 처음 화면으로 돌아가야 합니다.")
    })
    public ResponseEntity<SearchLogResponse> getSearchLogs(
            HttpServletRequest httpRequest
    ) {
        String userId = securityService.getUserId(httpRequest);
        List<SearchLogDto> searchLogs = searchLogService.getSearchLogs(userId);

        return new ResponseEntity<>(new SearchLogResponse(true, 200, "응답 성공", searchLogs), HttpStatus.OK);
    }

    @GetMapping("/api/search-logs/{searchLogId}/complete")
    @Operation(summary = "검색 조건으로 매물 추천 결과를 불러옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매물 응답 완료"),
            @ApiResponse(responseCode = "400", description = "path variable이 옳지 않은 경우"),
            @ApiResponse(responseCode = "404", description = "path variable로 입력한 id에 해당하는 검색 기록이 없을 경우")
    })
    public ResponseEntity<SearchResponse> getHouseList(
            @PathVariable int searchLogId,
            HttpServletRequest httpRequest
    ) {
        String userId = securityService.getUserId(httpRequest);
        SearchLog searchLog = searchLogService.getSearchLog(searchLogId);
        AllConditions allConditions = searchLog.toAllConditions();

        List<HouseWithCondition> result = conditionService.exec2(allConditions, userId);

        return new ResponseEntity<>(new SearchResponse(result, true, 200, "응답 . 성공"), HttpStatus.OK);
    }
}
