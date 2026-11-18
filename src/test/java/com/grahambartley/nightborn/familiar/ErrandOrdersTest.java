package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ErrandOrdersTest {

  @ParameterizedTest(name = "{0}, {1}")
  @CsvSource({"nightborn:errands, nightborn:wheel/carry"})
  @DisplayName("sending a familiar back to its errands is its own wheel action")
  void names(final String id, final String icon) {
    assertEquals(id, new ErrandOrders().id().toString());
    assertEquals(icon, ErrandOrders.ICON.toString());
  }
}
