package com.grahambartley.nightborn.servant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StanceTest {

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"0, FOLLOW", "1, STAY", "2, GUARD", "-1, FOLLOW", "3, FOLLOW"})
  @DisplayName("a wheel choice maps to its order, and anything outside the orders follows")
  void byIndex(final int index, final Stance expected) {
    assertEquals(expected, Stance.byIndex(index));
  }

  @ParameterizedTest(name = "{0} -> {1}, {2}")
  @CsvSource({
    "FOLLOW, nightborn.thrall.order.follow, nightborn:wheel/follow",
    "STAY, nightborn.thrall.order.stay, nightborn:wheel/stay",
    "GUARD, nightborn.thrall.order.guard, nightborn:wheel/guard"
  })
  @DisplayName("each order has its own label and icon on the command wheel")
  void wheel(final Stance mode, final String label, final String icon) {
    assertEquals(label, mode.labelKey());
    assertEquals(Identifier.of(icon), mode.icon());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "FOLLOW, nightborn.thrall.mode.follow",
    "STAY, nightborn.thrall.mode.stay",
    "GUARD, nightborn.thrall.mode.guard"
  })
  @DisplayName("each order names itself to the owner")
  void keys(final Stance mode, final String key) {
    assertEquals(key, mode.translationKey());
  }

  @ParameterizedTest(name = "\"{0}\" -> {1}")
  @CsvSource({"FOLLOW, FOLLOW", "STAY, STAY", "GUARD, GUARD", "'', FOLLOW", "nonsense, FOLLOW"})
  @DisplayName("a saved order loads back, and anything unknown follows")
  void byName(final String name, final Stance expected) {
    assertEquals(expected, Stance.byName(name));
  }
}
