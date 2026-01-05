package com.eatsfine.eatsfine.global.apiPayload;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.code.BaseSuccessCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"success", "code", "data", "message"})
public class ApiResponse<T> {

    @JsonProperty("success")
    private final Boolean isSuccess;

    @JsonProperty("code")
    private final String code;

    @JsonProperty("data")
    private T data;

    @JsonProperty("message")
    private final String message;


    // 성공한 경우 (data 포함)
    public static <T> ApiResponse<T> onSuccess(BaseSuccessCode code, T data) {
        return new ApiResponse<>(true, code.getCode(), data, code.getMessage());
    }

    // 실패한 경우 (data 포함)
    public static <T> ApiResponse<T> onFailure(BaseErrorCode code, T data) {
        return new ApiResponse<>(false, code.getCode(), data , code.getMessage());
    }
}
