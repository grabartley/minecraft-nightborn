package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PartyRollTest {

  @ParameterizedTest(name = "a party of {0} to {1} hunters stays in range over many rolls")
  @CsvSource({"4, 8", "1, 1", "6, 6", "1, 12"})
  void sizeStaysInItsRange(final int min, final int max) {
    final Random random = new Random(7);
    boolean sawMin = false;
    boolean sawMax = false;
    for (int i = 0; i < 2000; i++) {
      final int size = PartyRoll.size(random::nextInt, min, max);
      assertTrue(size >= min && size <= max, "size " + size);
      sawMin |= size == min;
      sawMax |= size == max;
    }
    assertTrue(sawMin && sawMax);
  }

  @ParameterizedTest(name = "an extra night chance of {0} with at most {1} nights")
  @CsvSource({"0, 3, 1, 1", "100, 3, 3, 3", "100, 1, 1, 1", "50, 3, 1, 3", "50, 2, 1, 2"})
  void nightsStayBetweenOneAndTheMost(
      final int chance, final int max, final int fewest, final int most) {
    final Random random = new Random(11);
    int low = Integer.MAX_VALUE;
    int high = 0;
    for (int i = 0; i < 2000; i++) {
      final int nights = PartyRoll.nights(random::nextInt, chance, max);
      low = Math.min(low, nights);
      high = Math.max(high, nights);
    }
    assertEquals(fewest, low);
    assertEquals(most, high);
  }

  @ParameterizedTest(name = "{0} members, {1} beds, {2}% asleep: {3} sleep")
  @CsvSource({
    "8, 3, 50, 3",
    "4, 3, 50, 2",
    "2, 3, 100, 1",
    "1, 3, 100, 0",
    "6, 3, 0, 0",
    "0, 3, 50, 0"
  })
  void someSleepAndAtLeastOneKeepsWatch(
      final int members, final int beds, final int share, final int sleepers) {
    assertEquals(sleepers, PartyRoll.sleepers(members, beds, share));
  }

  @ParameterizedTest(name = "a chance of {0} succeeds on a roll of {1}: {2}")
  @CsvSource({"0, 0, false", "100, 99, true", "20, 19, true", "20, 20, false"})
  void succeedsBelowTheChance(final int chance, final int roll, final boolean succeeds) {
    assertEquals(succeeds, PartyRoll.succeeds(bound -> roll, chance));
  }
}
