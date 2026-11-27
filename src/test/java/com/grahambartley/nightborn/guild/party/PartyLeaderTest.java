package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PartyLeaderTest {

  @ParameterizedTest(name = "seed {0}: the next area lies a fair walk away")
  @ValueSource(longs = {1L, 2L, 3L, 42L, 9001L})
  void movesOnToANewArea(final long seed) {
    final BlockPos from = new BlockPos(100, 64, -40);
    final BlockPos next = PartyLeader.onward(from, Random.create(seed));
    final double distance = Math.sqrt(from.getSquaredDistance(next));
    assertTrue(
        distance >= PartyLeader.MOVE_MIN - 1 && distance <= PartyLeader.MOVE_MAX + 1,
        "distance " + distance);
    assertTrue(next.getY() == from.getY());
  }

  @ParameterizedTest(name = "seed {0}: a day's travel carries on away from the last camp")
  @ValueSource(longs = {1L, 2L, 42L, 9001L})
  void travelCarriesOnAwayFromTheLastCamp(final long seed) {
    final BlockPos lastCamp = new BlockPos(0, 64, 0);
    final BlockPos from = new BlockPos(60, 64, 0);
    final BlockPos next = PartyLeader.beyond(lastCamp, from, Random.create(seed));
    final double step = Math.sqrt(from.getSquaredDistance(next));
    assertTrue(
        step >= PartyLeader.MOVE_MIN - 1 && step <= PartyLeader.MOVE_MAX + 1, "step " + step);
    assertTrue(
        lastCamp.getSquaredDistance(next) > lastCamp.getSquaredDistance(from),
        "further from the last camp: " + next);
  }

  @ParameterizedTest(
      name = "guarding {0}, left a camp {1}, dusk {2}, {3} blocks squared from the area: on {4}")
  @CsvSource({
    "false, true, false, 0, true",
    "false, true, false, 256, true",
    "false, true, false, 257, false",
    "false, true, true, 0, false",
    "false, false, false, 0, false",
    "true, true, false, 0, false"
  })
  void onlyAPassingPartyThatLeftACampTravelsOnByDay(
      final boolean guarding,
      final boolean leftACamp,
      final boolean dusk,
      final double squaredToArea,
      final boolean travels) {
    assertEquals(travels, PartyLeader.travels(guarding, leftACamp, dusk, squaredToArea));
  }
}
