package com.example.refapp.utils.errors;

public class RefAppException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public ErrorType errorType = ErrorType.GENERAL;

    public RefAppException() {
        super();
    }

    public RefAppException(ErrorType errorType) {
        this();
        this.errorType = errorType;
    }

    public RefAppException(ErrorType errorType, String detailMessage) {
        this(detailMessage);
        this.errorType = errorType;
    }

    public RefAppException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RefAppException(String detailMessage) {
        super(detailMessage);
    }

    public RefAppException(Throwable throwable) {
        super(throwable);
    }
}