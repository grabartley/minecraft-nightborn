package com.grahambartley.nightborn.moment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.testing.FakeAttachmentTarget;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class MomentAttachmentTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(Moment.class)
  @DisplayName("a player reads back the moment they are in until it is cleared")
  void readsBack(final Moment moment) {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    final MomentState state = new MomentState(moment, 10L, 0.0F, 100, 60);
    MomentAttachment.set(player, state);
    assertEquals(state, MomentAttachment.get(player));
    MomentAttachment.set(player, null);
    assertEquals(null, MomentAttachment.get(player));
  }

  static Stream<Arguments> rules() {
    return Stream.of(
        Arguments.of(
            "saved, so a reload can leave a player stuck",
            MomentAttachment.TYPE.isPersistent(),
            false),
        Arguments.of("kept through death", MomentAttachment.TYPE.copyOnDeath(), false),
        Arguments.of("sent to the player's client", MomentAttachment.TYPE.isSynced(), true));
  }

  @ParameterizedTest(name = "{0}: {2}")
  @MethodSource("rules")
  @DisplayName("a moment lives only while the player is online, and their client knows of it")
  void persistence(final String rule, final boolean actual, final boolean expected) {
    assertEquals(expected, actual, rule);
  }
}
