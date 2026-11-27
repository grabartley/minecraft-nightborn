package com.grahambartley.nightborn.dismember;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.DismembermentSettings;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.fight.Blow;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SeverRulesTest {

  private static final DismembermentSettings DEFAULTS = NightbornConfig.DEFAULTS.dismemberment();
  private static final Blow SWORD = new Blow(false, false, false, true, false);
  private static final Blow FIST = new Blow(false, false, false, false, false);
  private static final Blow STAKE = new Blow(false, true, false, false, true);

  private static SeverRules.Victim hunter(final float health, final boolean staggered) {
    return new SeverRules.Victim(true, false, 40.0F, health, staggered, false);
  }

  @ParameterizedTest(name = "health {0}, staggered {1}, dealt {2}, roll {3}: {4}")
  @CsvSource({
    "10, false, 10, 0.1, true",
    "13.9, false, 8, 0.1, true",
    "14, false, 8, 0.1, false",
    "30, true, 8, 0.1, true",
    "30, false, 8, 0.1, false",
    "10, false, 7.9, 0.1, false",
    "10, false, 10, 0.15, false",
    "10, false, 10, 0.149, true",
    "0, false, 10, 0.0, false"
  })
  @DisplayName("a sword cuts only a living open foe, with a strong blow and a lucky roll")
  void sword(
      final float health,
      final boolean staggered,
      final float taken,
      final double roll,
      final boolean severs) {
    assertEquals(
        severs,
        SeverRules.severs(DEFAULTS, hunter(health, staggered), SWORD, true, true, taken, roll));
  }

  @ParameterizedTest(name = "dealt {0}: {1}")
  @CsvSource({"19.9, false", "20, true", "39, true"})
  @DisplayName("a blow with no blade cuts only when it is massive")
  void massiveFist(final float taken, final boolean severs) {
    assertEquals(
        severs, SeverRules.severs(DEFAULTS, hunter(1.0F, true), FIST, true, true, taken, 0.0));
  }

  @ParameterizedTest(name = "dealt {0}")
  @CsvSource({"8", "20", "39"})
  @DisplayName("a stake never cuts, however hard it lands")
  void stakeNever(final float taken) {
    assertEquals(
        false, SeverRules.severs(DEFAULTS, hunter(1.0F, true), STAKE, true, true, taken, 0.0));
  }

  @ParameterizedTest(name = "maimable {0}, player {1}, players maimed {2}, by player {3}, pvp {4}")
  @CsvSource({
    "true, false, true, true, false, true",
    "false, false, true, true, true, false",
    "false, true, true, false, false, true",
    "false, true, false, false, true, false",
    "false, true, true, true, false, false",
    "false, true, true, true, true, true"
  })
  @DisplayName(
      "only maimable mobs and players lose parts, and a player's blow on a player needs PvP")
  void whoCanBeMaimed(
      final boolean maimable,
      final boolean player,
      final boolean playersMaimed,
      final boolean byPlayer,
      final boolean pvp,
      final boolean severs) {
    final DismembermentSettings settings =
        new DismembermentSettings(true, playersMaimed, 25, 20, 50, 35);
    final SeverRules.Victim victim =
        new SeverRules.Victim(maimable, player, 20.0F, 2.0F, false, false);
    assertEquals(severs, SeverRules.severs(settings, victim, SWORD, byPlayer, pvp, 10.0F, 0.0));
  }

  @ParameterizedTest(name = "enabled {0}, cut recently {1}: {2}")
  @CsvSource({"true, false, true", "false, false, false", "true, true, false"})
  @DisplayName("the switch and the cooldown both stop a cut")
  void switchAndCooldown(final boolean enabled, final boolean recent, final boolean severs) {
    final DismembermentSettings settings = new DismembermentSettings(enabled, true, 25, 20, 50, 35);
    final SeverRules.Victim victim = new SeverRules.Victim(true, false, 40.0F, 5.0F, false, recent);
    assertEquals(severs, SeverRules.severs(settings, victim, SWORD, true, true, 10.0F, 0.0));
  }

  @ParameterizedTest(name = "aimed {0}, already lost {1}: {2}")
  @CsvSource({
    "LEFT_ARM, , LEFT_ARM",
    "LEFT_ARM, LEFT_ARM, ",
    "RIGHT_LEG, LEFT_ARM, RIGHT_LEG",
    "HEAD, , ",
    ", , "
  })
  @DisplayName("the trunk, the head and a part already gone sever nothing")
  void part(final BodyPart aimed, final BodyPart lost, final BodyPart expected) {
    final BodyState body = lost == null ? BodyState.WHOLE : BodyState.WHOLE.cut(lost, 0L);
    assertEquals(Optional.ofNullable(expected), SeverRules.part(Optional.ofNullable(aimed), body));
  }
}
