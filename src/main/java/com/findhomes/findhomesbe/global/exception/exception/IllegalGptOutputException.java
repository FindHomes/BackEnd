package com.findhomes.findhomesbe.global.exception.exception;

public class IllegalGptOutputException extends RuntimeException {
    public IllegalGptOutputException() {
        super();
    }

    public IllegalGptOutputException(String message) {
        super(message);
    }

    public IllegalGptOutputException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalGptOutputException(Throwable cause) {
        super(cause);
    }

    protected IllegalGptOutputException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
