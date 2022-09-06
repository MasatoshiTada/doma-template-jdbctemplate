package com.example;

public record SqlParam<T>(String name, Class<T> valueType, T value) {
}
