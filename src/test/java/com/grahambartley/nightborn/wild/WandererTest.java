package com.grahambartley.nightborn.wild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class WandererTest {

  private static final UUID PLAYER = new UUID(0, 7);

  @ParameterizedTest(name = "last near {0}, now {1}, leave after {2}: {3}")
  @CsvSource({"0, 5999, 6000, false", "0, 6000, 6000, true", "100, 6050, 6000, false"})
  @DisplayName("a wild vampire leaves once no player has been near for the leave time")
  void leaves(final long lastNear, final long now, final long leaveTicks, final boolean leaves) {
    assertEquals(leaves, Wanderer.wild(lastNear).leaves(now, leaveTicks));
  }

  @ParameterizedTest(name = "seen at {0}")
  @CsvSource({"10", "6000"})
  @DisplayName("being seen restarts the leave time and keeps whom it watches")
  void seen(final long now) {
    final Wanderer seen = Wanderer.watching(PLAYER, 0L).seen(now);
    assertEquals(now, seen.lastNear());
    assertEquals(PLAYER, seen.watches().orElseThrow());
  }

  static Stream<Arguments> wanderers() {
    return Stream.of(
        Arguments.of(Wanderer.wild(0L), false),
        Arguments.of(Wanderer.wild(48_000L), false),
        Arguments.of(Wanderer.watching(PLAYER, 12L), true));
  }

  @ParameterizedTest(name = "{0} watcher {1}")
  @MethodSource("wanderers")
  @DisplayName("a wanderer survives a save and load and knows if it is a watcher")
  void roundTrip(final Wanderer wanderer, final boolean watcher) {
    assertEquals(watcher, wanderer.watcher());
    assertEquals(
        wanderer,
        Wanderer.CODEC
            .parse(
                JsonOps.INSTANCE,
                Wanderer.CODEC.encodeStart(JsonOps.INSTANCE, wanderer).getOrThrow())
            .getOrThrow());
  }
}
