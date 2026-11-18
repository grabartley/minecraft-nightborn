package com.grahambartley.nightborn.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.testing.FakeAttachmentTarget;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JournalLogAttachmentTest {

  static Stream<Arguments> stored() {
    return Stream.of(
        Arguments.of(JournalLog.NONE, false),
        Arguments.of(new JournalLog(Set.of(Journal.HUNTER)), true),
        Arguments.of(new JournalLog(Set.of(Journal.HUNTER, Journal.VAMPIRE)), true));
  }

  @ParameterizedTest(name = "{0} stores data: {1}")
  @MethodSource("stored")
  @DisplayName("a player reads back the journals given, and one given none costs nothing")
  void readsBack(final JournalLog log, final boolean storesData) {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    JournalLogAttachment.set(player, new JournalLog(Set.of(Journal.VAMPIRE)));
    JournalLogAttachment.set(player, log);
    assertEquals(log, JournalLogAttachment.get(player));
    assertEquals(storesData, player.hasAttached(JournalLogAttachment.TYPE));
  }

  static Stream<Arguments> rules() {
    return Stream.of(
        Arguments.of("saved with the player", JournalLogAttachment.TYPE.isPersistent(), true),
        Arguments.of("kept through death", JournalLogAttachment.TYPE.copyOnDeath(), true),
        Arguments.of("never sent to a client", JournalLogAttachment.TYPE.isSynced(), false));
  }

  @ParameterizedTest(name = "{0}: {2}")
  @MethodSource("rules")
  @DisplayName("the record of journals given lives on the server and survives death and reloads")
  void persistence(final String rule, final boolean actual, final boolean expected) {
    assertEquals(expected, actual, rule);
  }
}
