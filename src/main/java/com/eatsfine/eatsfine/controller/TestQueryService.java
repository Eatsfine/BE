package com.eatsfine.eatsfine.controller;

import com.eatsfine.eatsfine.global.apiPayload.code.GeneralErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestQueryService  {

    public void checkFlag(Long flag){
        if (flag == 1){
            throw new GeneralException(GeneralErrorCode.BAD_REQUEST);
        }
    }
}
