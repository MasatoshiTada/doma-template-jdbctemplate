package com.example;

/**
 * 2-way SQLに指定するパラメータを保持するクラスです。
 * valueTypeは冗長に思えます。
 * しかし、valueがnullの際でも型を示すために、必ずvalueTypeを指定してください。
 *
 * @param name パラメータ名
 * @param valueType パラメータ値の型
 * @param value パラメータ値
 * @see TwoWayJdbcTemplate
 * @author Masatoshi Tada (@suke_masa)
 */
public record SqlParam<T>(String name, Class<T> valueType, T value) {
}
