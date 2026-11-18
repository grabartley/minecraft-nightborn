package com.grahambartley.nightborn.wheel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WheelMenuTest {

  private static final Identifier ORDER = Identifier.of("nightborn", "thrall_order");
  private static final WheelEntry STAY =
      new WheelEntry(ORDER, 1, Identifier.ofVanilla("campfire"), Text.literal("Stay"), false);
  private static final WheelEntry GUARD =
      new WheelEntry(ORDER, 2, Identifier.ofVanilla("iron_sword"), Text.literal("Guard"), true);

  @ParameterizedTest(name = "entity {0}, {1} {2} -> {3}")
  @CsvSource({
    "7, nightborn:thrall_order, 1, 1",
    "7, nightborn:thrall_order, 2, 2",
    "7, nightborn:thrall_order, 0, -1",
    "8, nightborn:thrall_order, 1, -1",
    "7, nightborn:day_job, 1, -1"
  })
  @DisplayName("a choice counts only if the server showed that entry for that entity")
  void find(final int entity, final String action, final int argument, final int found) {
    final WheelMenu menu = new WheelMenu(7, List.of(STAY, GUARD));
    assertEquals(
        found < 0 ? Optional.empty() : Optional.of(found == 1 ? STAY : GUARD),
        menu.find(entity, Identifier.of(action), argument));
  }
}
