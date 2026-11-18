package com.grahambartley.nightborn.wild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.testing.FakeAttachmentTarget;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NightLogAttachmentTest {

  static Stream<NightLog> logs() {
    return Stream.of(
        NightLog.begun(0, 0, 5, 24_000L), NightLog.begun(9, 0, 5, 24_000L).watchedOnce());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("logs")
  @DisplayName("a player reads back their first nights, and has none until first seen")
  void readsBack(final NightLog log) {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    assertEquals(Optional.empty(), NightLogAttachment.get(player));
    NightLogAttachment.set(player, log);
    assertEquals(Optional.of(log), NightLogAttachment.get(player));
  }

  static Stream<Arguments> rules() {
    return Stream.of(
        Arguments.of("saved with the player", NightLogAttachment.TYPE.isPersistent(), true),
        Arguments.of("kept through death", NightLogAttachment.TYPE.copyOnDeath(), true),
        Arguments.of("never sent to a client", NightLogAttachment.TYPE.isSynced(), false));
  }

  @ParameterizedTest(name = "{0}: {2}")
  @MethodSource("rules")
  @DisplayName("a player's first nights live on the server and survive death and reloads")
  void persistence(final String rule, final boolean actual, final boolean expected) {
    assertEquals(expected, actual, rule);
  }
}
