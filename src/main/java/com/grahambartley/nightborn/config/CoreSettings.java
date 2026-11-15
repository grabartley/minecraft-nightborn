package com.grahambartley.nightborn.config;

import java.util.List;

public record CoreSettings(
    int bareHandDamage, int nightSpeed, int nightJump, int levitationDrainSeconds) {

  static final String SECTION = "core";

  public static final IntSetting BARE_HAND_DAMAGE =
      new IntSetting(SECTION, "bareHandDamage", 3, 0, 20, c -> c.core().bareHandDamage());
  public static final IntSetting NIGHT_SPEED =
      new IntSetting(SECTION, "nightSpeed", 20, 0, 100, c -> c.core().nightSpeed());
  public static final IntSetting NIGHT_JUMP =
      new IntSetting(SECTION, "nightJump", 20, 0, 100, c -> c.core().nightJump());
  public static final IntSetting LEVITATION_DRAIN_SECONDS =
      new IntSetting(
          SECTION, "levitationDrainSeconds", 20, 1, 600, c -> c.core().levitationDrainSeconds());

  public static final List<Setting<?>> SETTINGS =
      List.of(BARE_HAND_DAMAGE, NIGHT_SPEED, NIGHT_JUMP, LEVITATION_DRAIN_SECONDS);

  public CoreSettings {
    bareHandDamage = BARE_HAND_DAMAGE.clamp(bareHandDamage);
    nightSpeed = NIGHT_SPEED.clamp(nightSpeed);
    nightJump = NIGHT_JUMP.clamp(nightJump);
    levitationDrainSeconds = LEVITATION_DRAIN_SECONDS.clamp(levitationDrainSeconds);
  }

  static CoreSettings from(final SettingValues values) {
    return new CoreSettings(
        values.get(BARE_HAND_DAMAGE),
        values.get(NIGHT_SPEED),
        values.get(NIGHT_JUMP),
        values.get(LEVITATION_DRAIN_SECONDS));
  }
}
