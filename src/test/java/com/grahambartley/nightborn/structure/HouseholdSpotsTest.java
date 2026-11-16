package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HouseholdSpotsTest {

  private static List<BlockPos> row(final int length) {
    return IntStream.range(0, length).mapToObj(x -> new BlockPos(x, 64, 0)).toList();
  }

  @ParameterizedTest(name = "{1} wanted from {0} spots gives {2}")
  @CsvSource({"10, 3, 3", "10, 0, 0", "2, 5, 2", "0, 4, 0", "6, 6, 6"})
  @DisplayName("never chooses more spots than were asked for or exist")
  void counts(final int spots, final int wanted, final int chosen) {
    assertEquals(
        chosen, HouseholdSpots.choose(row(spots), wanted, Random.create(1L), List.of()).size());
  }

  @ParameterizedTest(name = "seed {0}")
  @CsvSource({"1", "2", "3", "42", "1000"})
  @DisplayName("never puts two residents in the same spot")
  void distinct(final long seed) {
    final List<BlockPos> chosen = HouseholdSpots.choose(row(4), 4, Random.create(seed), List.of());
    assertEquals(chosen.size(), new HashSet<>(chosen).size());
  }

  @ParameterizedTest(name = "seed {0}")
  @CsvSource({"1", "2", "3", "42", "1000"})
  @DisplayName("keeps residents apart while there is room to")
  void spreadsOut(final long seed) {
    final List<BlockPos> chosen = HouseholdSpots.choose(row(12), 3, Random.create(seed), List.of());
    for (final BlockPos a : chosen) {
      for (final BlockPos b : chosen) {
        assertTrue(a == b || a.getManhattanDistance(b) >= 2, a + " crowds " + b);
      }
    }
  }

  @ParameterizedTest(name = "seed {0}")
  @CsvSource({"7", "8", "9"})
  @DisplayName("the same seed gives the same household")
  void repeatable(final long seed) {
    assertEquals(
        HouseholdSpots.choose(row(12), 4, Random.create(seed), List.of()),
        HouseholdSpots.choose(row(12), 4, Random.create(seed), List.of()));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "0,70,0;3,64,0;0,64,4|0,70,0",
        "1,64,1;0,80,0;0,72,0|0,72,0",
        "5,64,5;-5,60,-5|-5,60,-5",
        "2,64,0|2,64,0"
      })
  @DisplayName("the head takes the spot nearest the middle across the floor, lowest floor first")
  void middle(final String spots, final String expected) {
    final List<BlockPos> floor =
        java.util.Arrays.stream(spots.split(";")).map(HouseholdSpotsTest::pos).toList();
    assertEquals(pos(expected), HouseholdSpots.middle(floor, new BlockPos(0, 70, 0)).orElseThrow());
  }

  @org.junit.jupiter.api.Test
  @DisplayName("an empty heart has no seat for a head")
  void noMiddle() {
    assertTrue(HouseholdSpots.middle(List.of(), BlockPos.ORIGIN).isEmpty());
  }

  @ParameterizedTest(name = "seed {0}")
  @CsvSource({"1", "2", "3", "42", "1000"})
  @DisplayName("residents never take or crowd a spot the head already holds")
  void keepsClearOfTheHead(final long seed) {
    final BlockPos head = new BlockPos(5, 64, 0);
    final List<BlockPos> chosen =
        HouseholdSpots.choose(row(12), 3, Random.create(seed), List.of(head));
    assertEquals(3, chosen.size());
    assertTrue(
        chosen.stream().allMatch(spot -> spot.getManhattanDistance(head) >= 2), chosen.toString());
  }

  private static BlockPos pos(final String xyz) {
    final String[] parts = xyz.split(",");
    return new BlockPos(
        Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
  }
}
