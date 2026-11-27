package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HunterPartyTest {

  private static final HunterParty.Camp CAMP =
      new HunterParty.Camp(
          new BlockPos(5, 64, 5), List.of(new BlockPos(6, 64, 5)), List.of(new BlockPos(6, 64, 6)));

  private static HunterParty party() {
    return HunterParty.gathered(
        List.of(UUID.randomUUID(), UUID.randomUUID()), new BlockPos(1, 64, 1), 100L);
  }

  @ParameterizedTest(name = "pitched {0}, guarding {1} survives a save and load")
  @CsvSource({"true, true", "true, false", "false, true", "false, false"})
  void survivesASaveAndLoad(final boolean pitched, final boolean guarding) {
    final HunterParty gathered =
        guarding ? party().guarding(Optional.of(new BlockPos(30, 64, 30))) : party();
    final HunterParty party = pitched ? gathered.pitch(CAMP, 2, 4L, 96_000L) : gathered;
    final HunterParty loaded =
        HunterParty.CODEC
            .parse(
                NbtOps.INSTANCE, HunterParty.CODEC.encodeStart(NbtOps.INSTANCE, party).getOrThrow())
            .getOrThrow();
    assertEquals(party, loaded);
  }

  @Test
  void pitchingCentresTheAreaOnTheFireAndStartsTheStay() {
    final HunterParty pitched = party().pitch(CAMP, 3, 9L, 216_500L);
    assertTrue(pitched.pitched());
    assertEquals(CAMP.fire(), pitched.area());
    assertEquals(new CampStay(3, 9L, 216_500L), pitched.stay());
    assertEquals(9L, pitched.lastTriedDay());
  }

  @Test
  void strikingCampMovesTheAreaOn() {
    final BlockPos next = new BlockPos(70, 64, 1);
    final HunterParty struck = party().pitch(CAMP, 1, 2L, 48_000L).struck(next);
    assertEquals(Optional.empty(), struck.camp());
    assertEquals(next, struck.area());
    assertEquals(CampStay.NONE, struck.stay());
    assertEquals(Optional.of(CAMP.fire()), struck.lastCamp());
  }

  @Test
  void aFailedPitchWalksOnAndWaitsForTheNextEvening() {
    final BlockPos next = new BlockPos(0, 64, 60);
    final HunterParty tried = party().pitch(CAMP, 1, 2L, 48_000L).struck(next).triedOn(7L, next);
    assertEquals(7L, tried.lastTriedDay());
    assertEquals(Optional.of(CAMP.fire()), tried.lastCamp());
    assertEquals(next, tried.area());
  }

  @Test
  void sightingsAndRaidRollsAreKept() {
    final HunterParty seen = party().seen(500L).raidRolled(3L);
    assertEquals(500L, seen.lastPlayerNear());
    assertEquals(3L, seen.lastRaidDay());
  }

  @Test
  void aSaveFromBeforeTheCampClockLoadsWithAnUnknownPitchTime() {
    final HunterParty pitched = party().pitch(CAMP, 2, 4L, 96_000L);
    final NbtCompound saved =
        (NbtCompound) HunterParty.CODEC.encodeStart(NbtOps.INSTANCE, pitched).getOrThrow();
    saved.remove("pitched_at");
    saved.remove("last_camp");
    final HunterParty loaded = HunterParty.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();
    assertEquals(new CampStay(2, 4L, CampStay.UNKNOWN), loaded.stay());
    assertEquals(Optional.empty(), loaded.lastCamp());
  }

  @ParameterizedTest(name = "guarding {0}")
  @CsvSource({"true", "false"})
  void guardingAVillageCentresTheAreaOnItsBellAndReleasingKeepsTheArea(final boolean guarding) {
    final BlockPos bell = new BlockPos(40, 64, -12);
    final HunterParty guarded = party().guarding(Optional.of(bell));
    final HunterParty party = guarding ? guarded : guarded.guarding(Optional.empty());
    assertEquals(guarding, party.guarding());
    assertEquals(bell, party.area());
  }
}
