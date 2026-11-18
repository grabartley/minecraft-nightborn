package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HunterPartyTest {

  private static final HunterParty.Camp CAMP =
      new HunterParty.Camp(
          new BlockPos(5, 64, 5), List.of(new BlockPos(6, 64, 5)), List.of(new BlockPos(6, 64, 6)));

  private static HunterParty party() {
    return HunterParty.gathered(
        List.of(UUID.randomUUID(), UUID.randomUUID()), new BlockPos(1, 64, 1), 100L);
  }

  @ParameterizedTest(name = "pitched {0} survives a save and load")
  @ValueSource(booleans = {true, false})
  void survivesASaveAndLoad(final boolean pitched) {
    final HunterParty party = pitched ? party().pitch(CAMP, 2, 4L) : party();
    final HunterParty loaded =
        HunterParty.CODEC
            .parse(
                NbtOps.INSTANCE, HunterParty.CODEC.encodeStart(NbtOps.INSTANCE, party).getOrThrow())
            .getOrThrow();
    assertEquals(party, loaded);
  }

  @Test
  void pitchingCentresTheAreaOnTheFireAndStartsTheStay() {
    final HunterParty pitched = party().pitch(CAMP, 3, 9L);
    assertTrue(pitched.pitched());
    assertEquals(CAMP.fire(), pitched.area());
    assertEquals(3, pitched.nights());
    assertEquals(9L, pitched.campDay());
    assertEquals(9L, pitched.lastTriedDay());
  }

  @Test
  void strikingCampMovesTheAreaOn() {
    final BlockPos next = new BlockPos(70, 64, 1);
    final HunterParty struck = party().pitch(CAMP, 1, 2L).struck(next);
    assertEquals(Optional.empty(), struck.camp());
    assertEquals(next, struck.area());
  }

  @Test
  void aFailedPitchWalksOnAndWaitsForTheNextEvening() {
    final BlockPos next = new BlockPos(0, 64, 60);
    final HunterParty tried = party().triedOn(7L, next);
    assertEquals(7L, tried.lastTriedDay());
    assertEquals(next, tried.area());
  }

  @Test
  void sightingsAndRaidRollsAreKept() {
    final HunterParty seen = party().seen(500L).raidRolled(3L);
    assertEquals(500L, seen.lastPlayerNear());
    assertEquals(3L, seen.lastRaidDay());
  }
}
