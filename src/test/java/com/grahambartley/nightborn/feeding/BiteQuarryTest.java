package com.grahambartley.nightborn.feeding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BiteQuarryTest {

  private static BiteQuarry quarry(
      final boolean bitable, final boolean struggles, final boolean finishable) {
    return new BiteQuarry() {
      @Override
      public boolean bitable() {
        return bitable;
      }

      @Override
      public boolean struggles() {
        return struggles;
      }

      @Override
      public boolean finishable() {
        return finishable;
      }
    };
  }

  @ParameterizedTest(name = "bitable {0}, struggles {1}, finishable {2}")
  @CsvSource({"false, false, false", "true, false, false", "true, true, true", "false, true, true"})
  @DisplayName("a quarry guards against bites, struggles and can be finished only as it says")
  void followsTheQuarry(final boolean bitable, final boolean struggles, final boolean finishable) {
    final BiteQuarry quarry = quarry(bitable, struggles, finishable);
    assertEquals(!bitable, BiteQuarry.guards(quarry));
    assertEquals(struggles, BiteQuarry.struggles(quarry));
    assertEquals(finishable, BiteQuarry.finishable(quarry));
  }

  @Test
  @DisplayName("any other creature is bitten, struggles and can be finished as before")
  void anyOtherCreature() {
    final BiteQuarry none = null;
    assertEquals(false, BiteQuarry.guards(none));
    assertEquals(true, BiteQuarry.struggles(none));
    assertEquals(true, BiteQuarry.finishable(none));
  }
}
