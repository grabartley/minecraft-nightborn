package com.grahambartley.nightborn.client.gore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.client.state.GoreLevel;
import com.grahambartley.nightborn.gore.Gore;
import net.minecraft.client.option.ParticlesMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class BloodBurstTest {

  @ParameterizedTest(name = "{2} with gore {0}, particles {1}: {3} drops, {4}")
  @CsvSource({
    "FULL, ALL, BITE, 8, FRESH",
    "FULL, DECREASED, BITE, 4, FRESH",
    "REDUCED, ALL, BITE, 4, DRIED",
    "REDUCED, DECREASED, BITE, 2, DRIED",
    "FULL, ALL, STAKED, 32, FRESH",
    "REDUCED, DECREASED, STAKED, 8, DRIED",
    "FULL, ALL, DRAINED, 14, FRESH",
    "REDUCED, DECREASED, DRAINED, 3, DRIED",
    "FULL, ALL, STAKE, 18, FRESH",
    "FULL, ALL, SILVER, 10, FRESH",
    "OFF, ALL, STAKED, 0, HIDDEN",
    "FULL, MINIMAL, STAKED, 0, HIDDEN"
  })
  @DisplayName("the gore option and the vanilla particles setting both cut the spray down")
  void follows(
      final GoreLevel gore,
      final ParticlesMode particles,
      final Gore kind,
      final int drops,
      final SplatterLook look) {
    final BloodBurst burst = BloodBurst.of(gore, particles, kind);
    assertEquals(drops, burst.drops());
    assertEquals(look, burst.look());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = Gore.class,
      names = {"DRAINED", "STAKED"})
  @DisplayName("only death effects leave a pool under the body")
  void deathsPool(final Gore kind) {
    assertEquals(1, BloodBurst.of(GoreLevel.FULL, ParticlesMode.ALL, kind).pools());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = Gore.class,
      names = {"BITE", "STAKE", "SILVER"})
  @DisplayName("hits leave no pool and no smoke")
  void hitsStayLight(final Gore kind) {
    final BloodBurst burst = BloodBurst.of(GoreLevel.FULL, ParticlesMode.ALL, kind);
    assertEquals(0, burst.pools());
    assertEquals(0, burst.smoke());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Gore.class)
  @DisplayName("only a staked vampire bursts all around and smokes")
  void stakedBursts(final Gore kind) {
    final BloodBurst burst = BloodBurst.of(GoreLevel.FULL, ParticlesMode.ALL, kind);
    assertEquals(kind == Gore.STAKED, burst.allAround());
    assertEquals(kind == Gore.STAKED, burst.smoke() > 0);
  }

  @ParameterizedTest(name = "gore {0}, particles {1}: {2} puffs")
  @CsvSource({
    "FULL, ALL, 12",
    "FULL, DECREASED, 6",
    "REDUCED, ALL, 6",
    "REDUCED, DECREASED, 3",
    "OFF, ALL, 0",
    "FULL, MINIMAL, 0"
  })
  @DisplayName("a staked vampire's smoke is cut down with its blood")
  void stakedSmokeFollowsSettings(
      final GoreLevel gore, final ParticlesMode particles, final int smoke) {
    assertEquals(smoke, BloodBurst.of(gore, particles, Gore.STAKED).smoke());
  }
}
