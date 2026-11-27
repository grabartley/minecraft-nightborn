package com.grahambartley.nightborn.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChunkHoldTest {

  @ParameterizedTest(name = "{0} holds {1} chunks out for {2} ticks")
  @CsvSource({"test_hold_near, 1, 20", "test_hold_far, 3, 100"})
  @DisplayName("a hold keeps its own named ticket, radius and expiry")
  void of(final String name, final int radius, final int expiry) {
    final ChunkHold hold = ChunkHold.of(name, radius, expiry);
    assertEquals(radius, hold.radius());
    assertEquals(name, hold.type().toString());
    assertEquals(expiry, hold.type().getExpiryTicks());
    assertNotEquals(ChunkHold.of(name + "_other", radius, expiry).type(), hold.type());
  }
}
