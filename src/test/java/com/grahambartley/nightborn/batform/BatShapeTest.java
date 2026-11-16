package com.grahambartley.nightborn.batform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BatShapeTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "'false', 'false'",
    "'true', 'true'",
    "'true,true', 'true,false'",
    "'true,false', 'true,true'",
    "'false,true,true,false', 'false,true,false,true'"
  })
  @DisplayName("the shape reports a change only on the tick bat form starts or ends")
  void changes(final String states, final String expected) {
    final BatShape shape = new BatShape();
    final StringBuilder seen = new StringBuilder();
    for (final String state : states.split(",")) {
      if (!seen.isEmpty()) {
        seen.append(',');
      }
      seen.append(shape.changed(Boolean.parseBoolean(state)));
    }
    assertEquals(expected, seen.toString());
  }
}
