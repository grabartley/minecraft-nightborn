package com.grahambartley.nightborn.wheel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WheelEntryTest {

  private static final Identifier ACTION = Identifier.of("nightborn", "thrall_order");

  @ParameterizedTest(name = "{0} {1} -> {2}")
  @CsvSource({
    "nightborn:thrall_order, 1, true",
    "nightborn:thrall_order, 2, false",
    "nightborn:scout, 1, false",
    "nightborn:thrall_order, -1, false"
  })
  @DisplayName("an entry answers only to its own action and argument")
  void names(final String action, final int argument, final boolean names) {
    final WheelEntry entry =
        new WheelEntry(ACTION, 1, Identifier.ofVanilla("lead"), Text.literal("Stay"), false);
    assertEquals(names, entry.names(Identifier.of(action), argument));
  }

  @ParameterizedTest(name = "current {0}")
  @CsvSource({"true", "false"})
  @DisplayName("an entry made without an argument carries the no-argument marker")
  void of(final boolean current) {
    final WheelEntry entry =
        WheelEntry.of(ACTION, Identifier.ofVanilla("lead"), Text.literal("Follow"), current);
    assertEquals(WheelEntry.NO_ARGUMENT, entry.argument());
    assertEquals(current, entry.current());
  }
}
