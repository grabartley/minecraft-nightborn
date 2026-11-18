package com.grahambartley.nightborn.thrall;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.servant.Stance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ThrallOrdersTest {

  @ParameterizedTest(name = "{0}, on a job {1}, shown {2} -> {3}")
  @CsvSource({
    "FOLLOW, false, FOLLOW, true",
    "FOLLOW, false, STAY, false",
    "GUARD, false, GUARD, true",
    "STAY, true, STAY, false",
    "STAY, true, FOLLOW, false"
  })
  @DisplayName("the wheel marks the thrall's order as current only while it has no day job")
  void current(final Stance mode, final boolean onAJob, final Stance shown, final boolean current) {
    assertEquals(current, ThrallOrders.current(mode, onAJob, shown));
  }

  @Test
  @DisplayName("the action is named in the mod's own namespace")
  void id() {
    assertEquals("nightborn:thrall_order", new ThrallOrders().id().toString());
  }
}
