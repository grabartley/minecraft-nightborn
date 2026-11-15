package com.grahambartley.nightborn.feeding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.blood.BloodQuality;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FeedingSessionTest {

  private static FeedingSession session() {
    return new FeedingSession(UUID.randomUUID(), BloodQuality.ANIMAL, 100);
  }

  @ParameterizedTest(name = "held at 100, now {0}: released {1}")
  @CsvSource({"100, false", "104, false", "108, false", "109, true", "200, true"})
  @DisplayName("letting go is noticed once the repeated use stops arriving")
  void release(final long now, final boolean released) {
    assertEquals(released, session().released(now));
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
}
