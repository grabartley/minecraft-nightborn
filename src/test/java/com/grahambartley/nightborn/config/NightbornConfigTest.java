package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NightbornConfigTest {

  static Stream<Arguments> prdStartingValues() {
    return Stream.of(
        Arguments.of(TurningSettings.SICKNESS_DAYS, 1),
        Arguments.of(TurningSettings.BITE_TURN_CHANCE, 100),
        Arguments.of(TurningSettings.BAT_TURN_CHANCE_PER_THOUSAND, 1),
        Arguments.of(TurningSettings.TURNING_BITE_BLOOD_COST, 50),
        Arguments.of(TurningSettings.OLD_BLOOD_REFILL_DAYS, 7),
        Arguments.of(FeedingSettings.FINISHER_HEALTH_PERCENT, 25),
        Arguments.of(FeedingSettings.FINISHER_DRAIN_TICKS, 30),
        Arguments.of(FeedingSettings.FINISHER_HEAL, 4),
        Arguments.of(DownedSettings.DOWNED_SECONDS, 30),
        Arguments.of(DownedSettings.ESCAPE_SECONDS, 60),
        Arguments.of(DownedSettings.RECOVER_HEALTH, 20));
  }

  @ParameterizedTest(name = "{0} = {1}")
  @MethodSource("prdStartingValues")
  @DisplayName("defaults match the starting values the PRD names")
  void defaultsMatchThePrd(final IntSetting setting, final int expected) {
    assertEquals(expected, setting.get(NightbornConfig.DEFAULTS));
  }

  @Test
  @DisplayName("hardcore vampire death and the player list tag are off by default")
  void optionalRulesAreOff() {
    assertEquals(false, DownedSettings.HARDCORE_VAMPIRE_DEATH.get(NightbornConfig.DEFAULTS));
    assertEquals(false, FactionSettings.SHOW_IN_PLAYER_LIST.get(NightbornConfig.DEFAULTS));
  }

  @Test
  @DisplayName("every setting has a unique id")
  void idsAreUnique() {
    final Set<String> ids = new HashSet<>();
    NightbornConfig.SETTINGS.forEach(setting -> assertTrue(ids.add(setting.id()), setting.id()));
  }

  static Stream<Setting<?>> everySetting() {
    return NightbornConfig.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("everySetting")
  @DisplayName("with changes one setting and keeps every other")
  void withChangesOneSetting(final Setting<?> changed) {
    final NightbornConfig updated = changeAway(changed);
    for (final Setting<?> setting : NightbornConfig.SETTINGS) {
      if (setting == changed) {
        assertTrue(!setting.get(updated).equals(setting.defaultValue()), setting.id());
      } else {
        assertEquals(setting.defaultValue(), setting.get(updated), setting.id());
      }
    }
  }

  @Test
  @DisplayName("sections are listed once each, in declaration order")
  void sectionsInOrder() {
    assertEquals(
        List.of(
            "turning",
            "feeding",
            "blood",
            "sun",
            "coffins",
            "downed",
            "core",
            "weakened",
            "wards",
            "factions",
            "skills",
            "minds",
            "gear",
            "coatings",
            "clothing",
            "mobs",
            "familiars",
            "servants",
            "combat",
            "structures",
            "parties",
            "ritual",
            "age"),
        NightbornConfig.sections());
  }

  private static NightbornConfig changeAway(final Setting<?> setting) {
    return switch (setting) {
      case IntSetting number ->
          NightbornConfig.DEFAULTS.with(
              number,
              number.defaultValue() == number.max()
                  ? number.defaultValue() - 1
                  : number.defaultValue() + 1);
      case BoolSetting flag -> NightbornConfig.DEFAULTS.with(flag, !flag.defaultValue());
    };
  }

  @Test
  @DisplayName("a screen save keeps changes made elsewhere while the screen was open")
  void screenSaveKeepsOtherChanges() {
    final NightbornConfig opened = NightbornConfig.DEFAULTS;
    final NightbornConfig current = opened.with(SunSettings.SUN_DAMAGE, 9);
    final NightbornConfig edited = opened.with(FeedingSettings.BOTTLE_SIZE, 6);
    final NightbornConfig merged = current.withChanges(opened, edited);
    assertEquals(9, merged.sun().sunDamage());
    assertEquals(6, merged.feeding().bottleSize());
    assertEquals(List.of(FeedingSettings.BOTTLE_SIZE), merged.changedFrom(current));
  }
}
