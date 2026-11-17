package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.network.SetPowerSlotPayload;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PowerDragTest {

  static final Identifier VANISH = Identifier.of("nightborn", "core/vanish");

  @ParameterizedTest(name = "from slot {0}, dropped on {1}: {2}")
  @CsvSource({
    "-1, 2, place 2",
    "-1, -1, nothing",
    "0, 3, place 3",
    "3, 3, nothing",
    "3, -1, clear 3"
  })
  @DisplayName(
      "a drop on a slot places the power, and a drop off the bar clears where it came from")
  void drop(final int from, final int over, final String outcome) {
    final Optional<SetPowerSlotPayload> expected =
        switch (outcome.split(" ")[0]) {
          case "place" -> Optional.of(new SetPowerSlotPayload(over, Optional.of(VANISH)));
          case "clear" -> Optional.of(new SetPowerSlotPayload(from, Optional.empty()));
          default -> Optional.empty();
        };
    assertEquals(expected, new PowerDrag(VANISH, from).drop(over));
  }

  @ParameterizedTest(name = "dropped on {0}")
  @CsvSource({"0", "4"})
  @DisplayName("a power dragged from the tree comes from no slot")
  void fromTree(final int over) {
    assertEquals(
        Optional.of(new SetPowerSlotPayload(over, Optional.of(VANISH))),
        PowerDrag.fromTree(VANISH).drop(over));
    assertEquals(Optional.empty(), PowerDrag.fromTree(VANISH).drop(PowerBarRow.NONE));
  }
}
