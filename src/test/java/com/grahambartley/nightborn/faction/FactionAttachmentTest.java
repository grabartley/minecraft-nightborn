package com.grahambartley.nightborn.faction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class FactionAttachmentTest {

  private static final class FakeTarget implements AttachmentTarget {
    private final Map<AttachmentType<?>, Object> attached = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <A> A getAttached(final AttachmentType<A> type) {
      return (A) attached.get(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A> A setAttached(final AttachmentType<A> type, final A value) {
      return (A) (value == null ? attached.remove(type) : attached.put(type, value));
    }

    @Override
    public boolean hasAttached(final AttachmentType<?> type) {
      return attached.containsKey(type);
    }
  }

  @Test
  @DisplayName("a player with nothing saved is human")
  void nothingSavedIsHuman() {
    assertEquals(Faction.HUMAN, FactionAttachment.get(new FakeTarget()));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Faction.class)
  @DisplayName("a player reads back the faction they were given")
  void readsBackTheFactionGiven(final Faction faction) {
    final FakeTarget player = new FakeTarget();
    FactionAttachment.set(player, faction);
    assertEquals(faction, FactionAttachment.get(player));
  }

  static Stream<Arguments> storedData() {
    return Stream.of(Arguments.of(Faction.HUMAN, false), Arguments.of(Faction.VAMPIRE, true));
  }

  @ParameterizedTest(name = "{0} stores data: {1}")
  @MethodSource("storedData")
  @DisplayName("only a vampire carries faction data, so a human costs nothing")
  void onlyVampireCarriesData(final Faction faction, final boolean stored) {
    final FakeTarget player = new FakeTarget();
    FactionAttachment.set(player, Faction.VAMPIRE);
    FactionAttachment.set(player, faction);
    assertEquals(stored, player.hasAttached(FactionAttachment.TYPE));
  }

  static Stream<Arguments> attachmentRules() {
    return Stream.<Arguments>of(
        Arguments.of(
            "saved with the player", (Predicate<AttachmentType<?>>) AttachmentType::isPersistent),
        Arguments.of(
            "kept through death", (Predicate<AttachmentType<?>>) AttachmentType::copyOnDeath),
        Arguments.of(
            "synced to a client", (Predicate<AttachmentType<?>>) AttachmentType::isSynced));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("attachmentRules")
  @DisplayName("the faction follows the persistence grid")
  void followsThePersistenceGrid(final String rule, final Predicate<AttachmentType<?>> holds) {
    assertTrue(holds.test(FactionAttachment.TYPE), "faction is not " + rule);
  }
}
