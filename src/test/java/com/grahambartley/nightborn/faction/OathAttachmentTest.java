package com.grahambartley.nightborn.faction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.testing.FakeAttachmentTarget;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OathAttachmentTest {

  static Stream<Arguments> stored() {
    return Stream.of(
        Arguments.of(Oath.NONE, false),
        Arguments.of(new Oath(true, false), true),
        Arguments.of(new Oath(true, true), true));
  }

  @ParameterizedTest(name = "{0} stores data: {1}")
  @MethodSource("stored")
  @DisplayName("a player reads back their oath, and one with none costs nothing")
  void readsBack(final Oath oath, final boolean storesData) {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    OathAttachment.set(player, new Oath(true, true));
    OathAttachment.set(player, oath);
    assertEquals(oath, OathAttachment.get(player));
    assertEquals(storesData, player.hasAttached(OathAttachment.TYPE));
  }

  static Stream<Arguments> rules() {
    return Stream.of(
        Arguments.of("saved with the player", OathAttachment.TYPE.isPersistent(), true),
        Arguments.of("kept through death", OathAttachment.TYPE.copyOnDeath(), true),
        Arguments.of("sent to clients", OathAttachment.TYPE.isSynced(), false));
  }

  @ParameterizedTest(name = "{0}: {2}")
  @MethodSource("rules")
  @DisplayName("the oath stays on the server and survives death and reloads")
  void persistence(final String rule, final boolean actual, final boolean expected) {
    assertEquals(expected, actual, rule);
  }
}
