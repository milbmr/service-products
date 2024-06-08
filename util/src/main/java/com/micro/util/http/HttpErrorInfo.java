package com.micro.util.http;

import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;

import lombok.Getter;

public class HttpErrorInfo {
    @Getter
    private final ZonedDateTime timestamp;
    @Getter
    private final String path;
    private final HttpStatus httpStatus;
    @Getter
    private final String message;

    public HttpErrorInfo() {
        timestamp = null;
        path = null;
        httpStatus = null;
        message = null;
    }

    public HttpErrorInfo(HttpStatus httpStatus, String path, String message) {
        timestamp = ZonedDateTime.now();
        this.path = path;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getStatus() {
        return httpStatus.value();
    }

    public String getError() {
        return httpStatus.getReasonPhrase();
    }
}
