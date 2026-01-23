package com.eatsfine.eatsfine.domain.businessnumber.service;

import com.eatsfine.eatsfine.domain.businessnumber.dto.BusinessNumberResDto;
import com.eatsfine.eatsfine.domain.businessnumber.exception.BusinessNumberException;
import com.eatsfine.eatsfine.domain.businessnumber.status.BusinessNumberErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@Profile("!local & !test")
@RequiredArgsConstructor
@Slf4j
public class RealBusinessNumberValidator implements BusinessNumberValidator {

    private final RestClient businessWebClient;

    @Value("${api.service-key}")
    private String serviceKey;

    @Override
    public void validate(String businessNumber, String startDate, String representativeName) {
        // 요청 바디
        Map<String, Object> body = Map.of(
                "businesses", List.of(
                        Map.of(
                                "b_no", businessNumber,
                                "start_dt", startDate,
                                "p_nm", representativeName,
                                "p_nm2", "",
                                "b_nm", "",
                                "corp_no", "",
                                "b_sector", "",
                                "b_type", "",
                                "b_adr", ""

                        )
                )
        );

        BusinessNumberResDto.BusinessNumberDto response = businessWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/validate")
                        .queryParam("serviceKey", serviceKey)
                        .build())
                .body(body)
                .retrieve()
                .onStatus(status -> status.isError(), (request, res) -> {
                    log.error("[BusinessNumber API] 통신 에러 발생: {}", res.getStatusCode());
                    throw new BusinessNumberException(BusinessNumberErrorStatus._API_COMMUNICATION_ERROR);
                })
                .body(BusinessNumberResDto.BusinessNumberDto.class);

        if(response == null || response.data() == null || response.data().isEmpty()) {
            log.error("[BusinessNumber API] 응답 데이터가 비어있습니다.");
            throw new BusinessNumberException(BusinessNumberErrorStatus._API_COMMUNICATION_ERROR);
        }

        List<String> invalidNumbers = response.data().stream()
                .filter(dto -> !"01".equals(dto.valid()))
                .map(dto -> dto.bNo())
                .toList();

        if(!invalidNumbers.isEmpty()) {
            log.warn("[BusinessNumber API] 유효하지 않은 사업자 번호 발견: {}", invalidNumbers);

            throw new BusinessNumberException(BusinessNumberErrorStatus._INVALID_BUSINESS_NUMBER);
        }

    };


}
