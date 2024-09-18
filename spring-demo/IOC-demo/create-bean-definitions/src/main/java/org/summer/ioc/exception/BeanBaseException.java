package org.summer.ioc.exception;

public class BeanBaseException extends RuntimeException{
    public BeanBaseException() {
    }

    public BeanBaseException(String message) {
        super(message);
    }

    public BeanBaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanBaseException(Throwable cause) {
        super(cause);
    }
}
