package com.example;

public record SqlParam<T>(String name, T value) {
    
    public Class<T> valueType() {
        return (Class<T>) value.getClass();
    }
}
