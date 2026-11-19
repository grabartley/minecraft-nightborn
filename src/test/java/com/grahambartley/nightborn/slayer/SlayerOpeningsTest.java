package com.grahambartley.nightborn.slayer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SlayerOpeningsTest {

  @ParameterizedTest(name = "pinned {0}, netted {1}, opened {2}: open {3}")
  @CsvSource({
    "false, false, false, false",
    "true, false, false, true",
    "false, true, false, true",
    "false, false, true, true",
    "true, true, true, true"
  })
  @DisplayName(
      "a vampire lies open when snared or pinned, netted, or staggered by a riposte or a dodge")
  void open(final boolean pinned, final boolean netted, final boolean opened, final boolean open) {
    assertEquals(open, SlayerOpenings.open(pinned, netted, opened));
  }
}
