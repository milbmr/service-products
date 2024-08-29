package com.micro.composite.product;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.micro.api.event.Event;

@SuppressWarnings("rawtypes")
public class IsSameEvent extends TypeSafeMatcher<String> {
  private final Logger LOG = LoggerFactory.getLogger(IsSameEvent.class);

  private ObjectMapper mapper = new ObjectMapper();

  private Event eventExpected;

  private IsSameEvent(Event eventExpected) {
    this.eventExpected = eventExpected;
  }

  @Override
  protected boolean matchesSafely(String eventAsJson) {
    if (eventExpected == null) {
      return false;
    }

    LOG.trace("creating map from json {}", eventAsJson);
    Map messageAsMap = createMapFromJson(eventAsJson);
    messageAsMap.remove("creationDate");

    Map eventAsMap = createMapFromEventWithoutCreationDate(eventExpected);

    LOG.trace("converted event to a map {}", messageAsMap);
    LOG.trace("compare event with the expected event ", eventAsMap);
    return messageAsMap.equals(eventAsMap);
  }

  @Override
  public void describeTo(Description description) {
    String eventAsString = convertEventToString(eventExpected);
    description.appendText("Event must look like " + eventAsString);
  }

  private String convertEventToString(Event expectedEvent) {
    try {
      return mapper.writeValueAsString(expectedEvent);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static Matcher<String> sameEventExcept(Event expectedEvent) {
    return new IsSameEvent(expectedEvent);
  }

  private Map createMapFromEventWithoutCreationDate(Event expectedEvent) {
    Map eventMap = createMapFromEvent(expectedEvent);
    eventMap.remove("creationDate");
    return eventMap;
  }

  private Map createMapFromEvent(Event expectedEvent) {
    // JsonNode node = mapper.convertValue(expectedEvent, JsonNode.class);
    return mapper.convertValue(expectedEvent, new TypeReference<HashMap>() {
    });
  }

  private Map createMapFromJson(String eventAsJson) {
    try {
      return mapper.readValue(eventAsJson, new TypeReference<HashMap>() {
      });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
