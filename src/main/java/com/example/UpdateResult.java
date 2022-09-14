package com.example;

/**
 * 更新行数と生成された主キーを保持するクラスです。
 *
 * @param updatedRows 更新行数
 * @param key 主キー
 * @see TwoWayJdbcTemplate#updateAndGetKey(String, String, SqlParam...)
 * @author Masatoshi Tada (@suke_masa)
 */
public record UpdateResult(int updatedRows, Number key) {
}
