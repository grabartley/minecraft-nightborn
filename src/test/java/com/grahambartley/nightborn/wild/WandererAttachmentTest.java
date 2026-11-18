package com.grahambartley.nightborn.wild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.testing.FakeAttachmentTarget;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class WandererAttachmentTest {

  static Stream<Wanderer> wanderers() {
    return Stream.of(Wanderer.wild(0L), Wanderer.watching(new UUID(0, 3), 20L));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("wanderers")
  @DisplayName("a vampire reads back the wanderer it was given, and none once settled")
  void readsBack(final Wanderer wanderer) {
    final FakeAttachmentTarget vampire = new FakeAttachmentTarget();
    assertEquals(Optional.empty(), WandererAttachment.get(vampire));
    WandererAttachment.set(vampire, wanderer);
    assertEquals(Optional.of(wanderer), WandererAttachment.get(vampire));
    WandererAttachment.clear(vampire);
    assertEquals(Optional.empty(), WandererAttachment.get(vampire));
  }

  static Stream<Arguments> rules() {
    return Stream.of(
        Arguments.of("saved with the vampire", WandererAttachment.TYPE.isPersistent(), true),
        Arguments.of("never sent to a client", WandererAttachment.TYPE.isSynced(), false));
  }

  @ParameterizedTest(name = "{0}: {2}")
  @MethodSource("rules")
  @DisplayName("a wanderer lives on the server and survives reloads")
  void persistence(final String rule, final boolean actual, final boolean expected) {
    assertEquals(expected, actual, rule);
  }
}
