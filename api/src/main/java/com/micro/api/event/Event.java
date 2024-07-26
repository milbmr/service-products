package com.micro.api.event;

import static java.time.ZonedDateTime.now;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;

public class Event<K, T> {
  public enum Type {
    CREATE,
    DELETE
  }

  private final Type eventType;
  private final K key;
  private final T data;
  private final ZonedDateTime creationDate;

  public Event() {
    this.eventType = null;
    this.key = null;
    this.data = null;
    this.creationDate = null;
  }

  public Event(Type evenType, K key, T data) {
    this.eventType = evenType;
    this.key = key;
    this.data = data;
    this.creationDate = now();
  }

  public Type getEventType() {
    return eventType;
  }

  public K getKey() {
    return this.key;
  }

  public T getData() {
    return this.data;
  }

  @JsonSerialize(using = ZonedDateTimeSerializer.class)
  public ZonedDateTime getCreationDate() {
    return creationDate;
  }
}
