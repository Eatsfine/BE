package com.eatsfine.eatsfine.domain.storetable.enums;

public enum SeatsType {
    GENERAL("일반석"),
    WINDOW("창가석"),
    ROOM("룸/프라이빗"),
    BAR("바(Bar)석"),
    OUTDOOR("야외석");

    private final String description;
    SeatsType(String description) { this.description = description; }
}
