package com.grahambartley.nightborn.feeding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DrinkingTest {

  @ParameterizedTest(name = "health {0}, rate {1}: take {2}, at last health {3}")
  @CsvSource({
    "20, 2, 2, false",
    "10, 2, 2, false",
    "3, 2, 2, true",
    "2.5, 2, 1.5, true",
    "1.5, 4, 0.5, true"
  })
  @DisplayName("a sip takes the rate each second but stops at the victim's last health")
  void sipsStopAtLastHealth(
      final float health, final int rate, final float take, final boolean atLast) {
    final Drinking.Sip sip = Drinking.next(health, rate, false);
    assertEquals(take, sip.take(), 0.0001F);
    assertEquals(false, sip.drains());
    assertEquals(atLast, sip.atLastHealth());
  }

  @Test
  @DisplayName("the second after reaching last health holds, so letting go leaves the victim alive")
  void reachingLastHealthHolds() {
    final Drinking.Sip sip = Drinking.next(1.0F, 2, false);
    assertEquals(0.0F, sip.take());
    assertEquals(true, sip.atLastHealth());
  }

  @ParameterizedTest(name = "health {0}")
  @CsvSource({"1.0", "0.5"})
  @DisplayName("holding past the last health drains the victim of everything left")
  void holdingOnDrains(final float health) {
    final Drinking.Sip sip = Drinking.next(health, 2, true);
    assertEquals(health, sip.take(), 0.0001F);
    assertEquals(true, sip.drains());
  }

  @Test
  @DisplayName("a dead victim gives nothing")
  void deadGivesNothing() {
    assertEquals(new Drinking.Sip(0.0F, false, false), Drinking.next(0.0F, 2, true));
  }

  @Test
  @DisplayName("a cow fed on from full is sipped to its last health, held, then drained")
  void wholeFeed() {
    float health = 10.0F;
    boolean atLast = false;
    int seconds = 0;
    boolean drained = false;
    while (!drained && seconds < 20) {
      final Drinking.Sip sip = Drinking.next(health, 2, atLast);
      health -= sip.take();
      atLast = sip.atLastHealth();
      drained = sip.drains();
      seconds++;
    }
    assertEquals(0.0F, health, 0.0001F);
    assertEquals(6, seconds);
  }
}
