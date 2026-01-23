package com.eatsfine.eatsfine.domain.businessnumber.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "test"})
public class MockBusinessNumberValidator implements BusinessNumberValidator {
    @Override
    public void validate(String businessNumber, String startDate, String representativeName) {
        // pass
    }
}
