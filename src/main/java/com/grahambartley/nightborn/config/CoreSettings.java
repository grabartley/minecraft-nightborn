package com.grahambartley.nightborn.config;

import java.util.List;

public record CoreSettings(
    int bareHandDamage,
    int nightSpeed,
    int nightJump,
    int levitationDrainSeconds,
    int batDrainSeconds,
    int batHeavyDamage) {

  static final String SECTION = "core";

  public static final IntSetting BARE_HAND_DAMAGE =
      new IntSetting(SECTION, "bareHandDamage", 3, 0, 20, c -> c.core().bareHandDamage());
  public static final IntSetting NIGHT_SPEED =
      new IntSetting(SECTION, "nightSpeed", 20, 0, 100, c -> c.core().nightSpeed());
  public static final IntSetting NIGHT_JUMP =
      new IntSetting(SECTION, "nightJump", 20, 0, 100, c -> c.core().nightJump());
  public static final IntSetting LEVITATION_DRAIN_SECONDS =
      new IntSetting(
          SECTION, "levitationDrainSeconds", 120, 1, 600, c -> c.core().levitationDrainSeconds());
  public static final IntSetting BAT_DRAIN_SECONDS =
      new IntSetting(SECTION, "batDrainSeconds", 30, 1, 600, c -> c.core().batDrainSeconds());
  public static final IntSetting BAT_HEAVY_DAMAGE =
      new IntSetting(SECTION, "batHeavyDamage", 6, 1, 100, c -> c.core().batHeavyDamage());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          BARE_HAND_DAMAGE,
          NIGHT_SPEED,
          NIGHT_JUMP,
          LEVITATION_DRAIN_SECONDS,
          BAT_DRAIN_SECONDS,
          BAT_HEAVY_DAMAGE);

  public CoreSettings {
    bareHandDamage = BARE_HAND_DAMAGE.clamp(bareHandDamage);
    nightSpeed = NIGHT_SPEED.clamp(nightSpeed);
    nightJump = NIGHT_JUMP.clamp(nightJump);
    levitationDrainSeconds = LEVITATION_DRAIN_SECONDS.clamp(levitationDrainSeconds);
    batDrainSeconds = BAT_DRAIN_SECONDS.clamp(batDrainSeconds);
    batHeavyDamage = BAT_HEAVY_DAMAGE.clamp(batHeavyDamage);
  }

  static CoreSettings from(final SettingValues values) {
    return new CoreSettings(
        values.get(BARE_HAND_DAMAGE),
        values.get(NIGHT_SPEED),
        values.get(NIGHT_JUMP),
        values.get(LEVITATION_DRAIN_SECONDS),
        values.get(BAT_DRAIN_SECONDS),
        values.get(BAT_HEAVY_DAMAGE));
  }
}
