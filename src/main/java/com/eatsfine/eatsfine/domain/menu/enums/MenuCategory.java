package com.eatsfine.eatsfine.domain.menu.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MenuCategory {

    MAIN("메인"),
    SIDE("사이드"),
    BEVERAGE("음료"),
    ALCOHOL("주류");


    private final String description;
}
