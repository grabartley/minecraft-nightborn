package com.grahambartley.nightborn.fight;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BlowsTest {

  private static final UUID FOE = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @ParameterizedTest(name = "struck at {0}, asked at {1}, window {2}: recent {3}")
  @CsvSource({"100, 150, 200, true", "100, 300, 200, true", "100, 301, 200, false"})
  @DisplayName("a blow counts as recent within its window")
  void recent(final long at, final long now, final int window, final boolean recent) {
    assertEquals(recent, Blows.NONE.with(FOE, at, 1200).recent(FOE, now, window));
  }

  @ParameterizedTest(name = "first at {0}, second at {1}, keep {2}: first kept {3}")
  @CsvSource({"0, 100, 1200, true", "0, 1300, 1200, false"})
  @DisplayName("old blows are forgotten when a new one is noted")
  void forgets(final long first, final long second, final int keep, final boolean kept) {
    final Blows blows = Blows.NONE.with(OTHER, first, keep).with(FOE, second, keep);
    assertEquals(kept, blows.lastAt().containsKey(OTHER));
  }
}
