package com.eatsfine.eatsfine.domain.businessnumber.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

public class BusinessNumberResDto {

    @Builder
    public record BusinessNumberDto(
            @JsonProperty("status_code")
            String statusCode,
            @JsonProperty("request_cnt")
            int requestCnt,
            @JsonProperty("valid_cnt")
            int validCnt,
            List<BusinessDataDto> data
    ){
        public record BusinessDataDto(
                @JsonProperty("b_no")
                String bNo,
                String valid
        ){}
    }
}
