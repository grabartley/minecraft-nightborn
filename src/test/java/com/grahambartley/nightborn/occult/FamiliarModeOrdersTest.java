package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FamiliarModeOrdersTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({"nightborn:familiar_mode"})
  @DisplayName("a ghostly familiar's tasks answer to one action, told apart by the task")
  void id(final String id) {
    assertEquals(id, new FamiliarModeOrders().id().toString());
  }
}
