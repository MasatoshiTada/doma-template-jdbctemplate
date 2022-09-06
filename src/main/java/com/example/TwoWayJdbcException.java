package com.example;

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
