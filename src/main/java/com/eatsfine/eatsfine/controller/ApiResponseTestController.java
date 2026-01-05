package com.eatsfine.eatsfine.controller;

import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import com.eatsfine.eatsfine.global.apiPayload.code.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ApiResponseTestController {

    private final TestQueryService testQueryService;

    @GetMapping("/test")
    public ApiResponse<String> test() {
            // 응답 코드 정의
            GeneralSuccessCode code = GeneralSuccessCode.OK;

            return ApiResponse.onSuccess(
                    code,
                    "success response"
            );
    }

        // 예외 상황
        @GetMapping("/exception")
        public ApiResponse<String> exception(
                @RequestParam Long flag
        ) {

            testQueryService.checkFlag(flag);

            // 응답 코드 정의
            GeneralSuccessCode code = GeneralSuccessCode.OK;
            return ApiResponse.onSuccess(code, "exception ");
        }
}

