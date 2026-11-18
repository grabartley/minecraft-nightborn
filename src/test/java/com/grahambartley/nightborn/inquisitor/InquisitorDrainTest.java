package com.grahambartley.nightborn.inquisitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InquisitorDrainTest {

  @ParameterizedTest(name = "drain of {0} seconds: kneels with {1}")
  @CsvSource({"2, 2", "5, 5", "30, 30", "1, 2", "0, 2"})
  @DisplayName("on his knees he holds one sip of life for each second the drain takes")
  void downedHealth(final int seconds, final float health) {
    assertEquals(health, InquisitorDrain.downedHealth(seconds));
  }

  @ParameterizedTest(name = "health {0}, bite {1}: takes {2}")
  @CsvSource({"5, 2, 1", "2, 1, 1", "2, 0.5, 0.5", "1, 1, 1", "1, 2, 1", "4, 4, 4"})
  @DisplayName("each sip takes one, and the last sip takes what is left")
  void taken(final float health, final float bite, final float taken) {
    assertEquals(taken, InquisitorDrain.taken(health, bite));
  }

  @ParameterizedTest(
      name = "bite {0}, from a vampire {1}, from the turned human {2}, from outside {3}: ends {4}")
  @CsvSource({
    "true, true, false, false, true",
    "true, false, false, false, false",
    "false, true, false, false, false",
    "false, false, true, false, true",
    "false, false, false, false, false",
    "true, true, false, true, false",
    "false, false, true, true, false"
  })
  @DisplayName(
      "on his knees only a vampire's bite or the turned-on human's blow ends him, and never from"
          + " outside the arena")
  void ends(
      final boolean bitten,
      final boolean vampire,
      final boolean turned,
      final boolean outside,
      final boolean ends) {
    assertEquals(ends, InquisitorDrain.ends(bitten, vampire, turned, outside));
  }

  @ParameterizedTest(name = "a drain of {0} seconds")
  @CsvSource({"2", "5", "12", "30"})
  @DisplayName("a held bite with sips of two drains him in as many sips as the drain has seconds")
  void sipsMatchTheSetting(final int seconds) {
    float health = InquisitorDrain.downedHealth(seconds);
    boolean atLast = false;
    int sips = 0;
    while (health > 0.0F && sips < 100) {
      sips++;
      if (health <= 1.0F) {
        if (atLast) {
          health -= InquisitorDrain.taken(health, health);
        }
        atLast = true;
        continue;
      }
      final float sip = Math.min(2.0F, health - 1.0F);
      atLast = health - sip <= 1.0F;
      health -= InquisitorDrain.taken(health, sip);
    }
    assertEquals(seconds, sips);
  }
}
