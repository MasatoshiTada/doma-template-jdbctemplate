package com.example;

public record SqlParam(String name, Class<? extends Object> valueType, Object value) {
}
