package com.example;

/**
 * SQLファイル関連の例外です。
 *
 * @author Masatoshi Tada (@suke_masa)
 * @see TwoWayJdbcTemplate
 */
public class TwoWayJdbcException extends RuntimeException {

    public TwoWayJdbcException(String message) {
        super(message);
    }

    public TwoWayJdbcException(Throwable cause) {
        super(cause);
    }

    public TwoWayJdbcException(String message, Throwable cause) {
        super(message, cause);
    }
}
