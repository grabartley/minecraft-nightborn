package com.grahambartley.nightborn.feeding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.blood.BloodQuality;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FeedingSessionTest {

  private static FeedingSession session() {
    return FeedingSession.bite(UUID.randomUUID(), BloodQuality.ANIMAL, Optional.empty(), 100);
  }

  @ParameterizedTest(name = "held at 100, now {0}: released {1}")
  @CsvSource({"100, false", "104, false", "108, false", "109, true", "200, true"})
  @DisplayName("letting go is noticed once the repeated use stops arriving")
  void release(final long now, final boolean released) {
    assertEquals(released, session().released(now));
  }

  @ParameterizedTest(name = "first {0}, then {1}")
  @CsvSource({"1.0, 2.0", "0.0, 0.0", "-4.5, 9.0"})
  @DisplayName("the anchor is the first position given and never moves")
  void anchorStaysWhereTheBiteBegan(final double first, final double then) {
    final FeedingSession session = session();
    assertEquals(new Vec3d(first, 64.0, first), session.anchorAt(new Vec3d(first, 64.0, first)));
    assertEquals(new Vec3d(first, 64.0, first), session.anchorAt(new Vec3d(then, 70.0, then)));
  }

  @Test
  @DisplayName("holding on keeps the session alive")
  void holdingKeepsItAlive() {
    final FeedingSession session = session();
    session.held(150);
    assertFalse(session.released(155));
  }

  @Test
  @DisplayName("a drink falls due once a second, starting one second after the bite")
  void drinksEverySecond() {
    final FeedingSession session = session();
    assertFalse(session.drinkDue(119));
    assertTrue(session.drinkDue(120));
    assertFalse(session.drinkDue(121));
    assertFalse(session.drinkDue(139));
    assertTrue(session.drinkDue(140));
  }

  @Test
  @DisplayName("each witness is alerted once and the bell rings once")
  void witnessesOnce() {
    final FeedingSession session = session();
    final UUID villager = UUID.randomUUID();
    assertTrue(session.firstSighting(villager));
    assertFalse(session.firstSighting(villager));
    assertFalse(session.bellRung());
    session.markBellRung();
    assertTrue(session.bellRung());
  }

  @Test
  @DisplayName("the turn is rolled once per feeding, so another bite cannot stack chances")
  void turnRolledOnce() {
    final FeedingSession session = session();
    assertTrue(session.rollTurnOnce());
    assertFalse(session.rollTurnOnce());
  }

  @Test
  @DisplayName("the total taken adds up every sip")
  void totals() {
    final FeedingSession session = session();
    session.took(2.0F);
    session.took(1.5F);
    assertEquals(3.5F, session.totalTaken(), 0.0001F);
  }

  @ParameterizedTest(name = "drain of {0} ticks begun at 100, now {1}: due {2}")
  @CsvSource({"30, 129, false", "30, 130, true", "30, 500, true", "0, 101, true", "5, 104, false"})
  @DisplayName("a finisher drains once its time is up, never sips, and never lets go early")
  void finisher(final int drainTicks, final long now, final boolean due) {
    final FeedingSession finisher =
        FeedingSession.finisher(
            UUID.randomUUID(), BloodQuality.VILLAGER, Optional.empty(), 100, drainTicks);
    assertTrue(finisher.finisher());
    assertEquals(due, finisher.finisherDue(now));
    assertFalse(finisher.drinkDue(now));
    assertFalse(finisher.released(now + 1000));
  }

  @ParameterizedTest(name = "gift {0}")
  @CsvSource({"nightborn:horse", "nightborn:wolf"})
  @DisplayName("a plain bite is never a finisher and carries the prey's gift")
  void plainBite(final String gift) {
    final FeedingSession bite =
        FeedingSession.bite(
            UUID.randomUUID(), BloodQuality.ANIMAL, Optional.of(Identifier.of(gift)), 100);
    assertFalse(bite.finisher());
    assertFalse(bite.finisherDue(10_000));
    assertEquals(Optional.of(Identifier.of(gift)), bite.gift());
  }

  @ParameterizedTest(name = "biter first at {0}, then {1}")
  @CsvSource({"1.0, 2.0", "-3.0, 8.0"})
  @DisplayName("the biter's anchor is where the bite began")
  void biterAnchor(final double first, final double then) {
    final FeedingSession session = session();
    assertEquals(
        new Vec3d(first, 64.0, first), session.biterAnchorAt(new Vec3d(first, 64.0, first)));
    assertEquals(new Vec3d(first, 64.0, first), session.biterAnchorAt(new Vec3d(then, 64.0, then)));
  }
}
