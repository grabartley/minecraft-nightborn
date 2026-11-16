package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SeatingTest {

  @ParameterizedTest(name = "sneaking={0} riding={1} taken={2} -> {3}")
  @CsvSource({
    "false, false, false, SIT",
    "false, false, true, TAKEN",
    "true, false, false, PASS",
    "false, true, false, PASS",
    "true, true, true, PASS"
  })
  void seatsOnlyAStandingPlayerOnAFreeChair(
      final boolean sneaking,
      final boolean riding,
      final boolean taken,
      final Seating.Choice expected) {
    assertEquals(expected, Seating.choose(sneaking, riding, taken));
  }

  @ParameterizedTest(name = "sitter={0} chair={1} keeps={2}")
  @CsvSource({
    "true, true, true",
    "false, true, false",
    "true, false, false",
    "false, false, false"
  })
  void keepsTheSeatOnlyWhileSomeoneSitsOnAChair(
      final boolean sitter, final boolean chair, final boolean keeps) {
    assertEquals(keeps, Seating.keepsSeat(sitter, chair));
  }

  @ParameterizedTest(name = "seat {0}, head {1}: {2}")
  @CsvSource({
    "0, 0, 0",
    "0, 45, 45",
    "0, 90, 60",
    "0, -120, -60",
    "180, 170, 170",
    "180, -170, 190",
    "90, 260, 150"
  })
  void aSeatedMobFacesTheChairAndTurnsItsHeadOnlySoFar(
      final float seat, final float head, final float expected) {
    assertEquals(expected, Seating.headYaw(seat, head), 1.0E-4f);
  }

  @ParameterizedTest(name = "facing {0}: {1}")
  @CsvSource({
    "NORTH, NORTH EAST WEST SOUTH",
    "SOUTH, SOUTH WEST EAST NORTH",
    "EAST, EAST SOUTH NORTH WEST",
    "WEST, WEST NORTH SOUTH EAST"
  })
  void aSitterStepsOffTheFrontFirstThenTheSidesThenTheBack(
      final Direction facing, final String order) {
    assertEquals(
        Arrays.stream(order.split(" ")).map(Direction::valueOf).toList(),
        Seating.dismountSides(facing));
  }
}
