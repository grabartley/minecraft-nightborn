package com.grahambartley.nightborn.client.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EldestSetTooltipTest {

  @ParameterizedTest(name = "blade {0}, armour {1}: own line {2}, set line {3}")
  @CsvSource({"true, false, true, true", "false, true, false, true", "false, false, false, false"})
  @DisplayName("the blade explains its own power, and every piece of the set explains the bonus")
  void lines(final boolean blade, final boolean armor, final boolean own, final boolean set) {
    assertEquals(
        own ? Optional.of("nightborn.eldest_blade.tooltip") : Optional.empty(),
        EldestSetTooltip.own(blade));
    assertEquals(
        set ? Optional.of("nightborn.eldest_set.tooltip") : Optional.empty(),
        EldestSetTooltip.shared(blade, armor));
  }
}
