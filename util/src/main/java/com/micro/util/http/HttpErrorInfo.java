package com.micro.util.http;

import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;

public class HttpErrorInfo {
  private final ZonedDateTime timestamp;
  private final String path;
  private final HttpStatus httpStatus;
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

  public ZonedDateTime getTimestamp() {
    return timestamp;
  }

  public String getPath() {
    return path;
  }

  public int getStatus() {
    return httpStatus.value();
  }

  public String getError() {
    return httpStatus.getReasonPhrase();
  }

  public String getMessage() {
    return message;
  }
}
