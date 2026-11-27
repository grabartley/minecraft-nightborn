package com.grahambartley.nightborn.guild.party;

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

  @ParameterizedTest(name = "seed {0}: a guard party moves {1} to {2} blocks along")
  @CsvSource({"1, 24, 32", "42, 24, 32", "9001, 24, 32", "7, 5, 5"})
  void aGuardPartyPitchesAFreshCampNearby(final long seed, final int min, final int max) {
    final BlockPos from = new BlockPos(-12, 70, 300);
    final BlockPos next = PartyLeader.onward(from, Random.create(seed), min, max);
    final double distance = Math.sqrt(from.getSquaredDistance(next));
    assertTrue(distance >= min - 1 && distance <= max + 1, "distance " + distance);
    assertTrue(next.getY() == from.getY());
  }
}
