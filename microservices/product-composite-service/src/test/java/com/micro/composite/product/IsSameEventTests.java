package com.micro.composite.product;

import static com.micro.api.event.Event.Type.CREATE;
import static com.micro.api.event.Event.Type.DELETE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static com.micro.composite.product.IsSameEvent.sameEventExcept;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.micro.api.core.product.Product;
import com.micro.api.event.Event;

@SuppressWarnings("rawtypes")
public class IsSameEventTests {

  ObjectMapper mapper = new ObjectMapper();

  @Test
  public void isSameEvent() throws JsonProcessingException {
    Event event1 = new Event<>(CREATE, 1, new Product(1, "name", 2, null));
    Event event2 = new Event<>(CREATE, 1, new Product(1, "name", 2, null));
    Event event3 = new Event<>(CREATE, 1, null);
    Event event4 = new Event<>(DELETE, 1, new Product(1, "name", 2, null));

    String eventAsString = mapper.writeValueAsString(event1);

    assertThat(eventAsString, is(sameEventExcept(event2)));
    assertThat(eventAsString, not(sameEventExcept(event3)));
    assertThat(eventAsString, not(sameEventExcept(event4)));
  }

}
