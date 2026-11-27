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

  private static final BlockPos BELL = new BlockPos(40, 64, -12);

  @ParameterizedTest(name = "guarding {0}: pitching for 2 nights gives a guard stay {0}")
  @CsvSource({"true", "false"})
  void aGuardPartysCampHasNoNightLimit(final boolean guarding) {
    final HunterParty gathered = guarding ? party().guarding(Optional.of(BELL)) : party();
    final HunterParty pitched = gathered.pitch(CAMP, 2, 9L, 216_500L);
    assertEquals(
        guarding ? CampStay.guarding(BELL, 9L, 216_500L) : new CampStay(2, 9L, 216_500L),
        pitched.stay());
  }

  @ParameterizedTest(name = "pitched {0}: relieved guards keep their camp {0} and its stay ends")
  @CsvSource({"true", "false"})
  void relievedGuardsBecomeAPassingParty(final boolean pitched) {
    final HunterParty guarded = party().guarding(Optional.of(BELL));
    final HunterParty party = pitched ? guarded.pitch(CAMP, 1, 9L, 216_500L) : guarded;
    final HunterParty relieved = party.relieved(220_000L);
    assertEquals(false, relieved.guarding());
    assertEquals(pitched, relieved.pitched());
    assertEquals(pitched ? 220_000L : CampStay.UNKNOWN, relieved.stay().releasedAt());
  }

  @ParameterizedTest(name = "pitched {0}, guarding {1}: an older save loads with a guard stay {2}")
  @CsvSource({"true, true, true", "true, false, false", "false, true, false"})
  void aGuardCampFromAnOlderSaveBecomesOpenEnded(
      final boolean pitched, final boolean guarding, final boolean guardStay) {
    final HunterParty gathered = guarding ? party().guarding(Optional.of(BELL)) : party();
    final HunterParty party = pitched ? gathered.pitch(CAMP, 2, 4L, 96_000L) : gathered;
    final NbtCompound saved =
        (NbtCompound) HunterParty.CODEC.encodeStart(NbtOps.INSTANCE, party).getOrThrow();
    saved.remove("guard_bell");
    if (pitched) {
      saved.putInt("nights", 2);
    }
    final HunterParty loaded = HunterParty.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();
    assertEquals(guardStay, loaded.stay().forGuard());
    assertEquals(pitched && !guarding ? 2 : party.stay().nights(), loaded.stay().nights());
  }

  @Test
  void headingOnChangesOnlyTheArea() {
    final BlockPos next = new BlockPos(150, 64, 1);
    final HunterParty struck = party().pitch(CAMP, 1, 2L, 48_000L).struck(new BlockPos(70, 64, 1));
    final HunterParty heading = struck.heading(next);
    assertEquals(next, heading.area());
    assertEquals(struck.lastCamp(), heading.lastCamp());
    assertEquals(struck.lastTriedDay(), heading.lastTriedDay());
  }
}
