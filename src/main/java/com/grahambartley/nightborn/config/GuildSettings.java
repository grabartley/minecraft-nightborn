package com.grahambartley.nightborn.config;

import java.util.List;

public record GuildSettings(boolean riteNeedsOath, int riteCooldownSeconds, int riteShieldSeconds) {

  static final String SECTION = "guild";

  public static final BoolSetting RITE_NEEDS_OATH =
      new BoolSetting(SECTION, "riteNeedsOath", true, c -> c.guild().riteNeedsOath());
  public static final IntSetting RITE_COOLDOWN_SECONDS =
      new IntSetting(
          SECTION, "riteCooldownSeconds", 600, 0, 7200, c -> c.guild().riteCooldownSeconds());
  public static final IntSetting RITE_SHIELD_SECONDS =
      new IntSetting(SECTION, "riteShieldSeconds", 3, 0, 30, c -> c.guild().riteShieldSeconds());

  public static final List<Setting<?>> SETTINGS =
      List.of(RITE_NEEDS_OATH, RITE_COOLDOWN_SECONDS, RITE_SHIELD_SECONDS);

  public GuildSettings {
    riteCooldownSeconds = RITE_COOLDOWN_SECONDS.clamp(riteCooldownSeconds);
    riteShieldSeconds = RITE_SHIELD_SECONDS.clamp(riteShieldSeconds);
  }

  static GuildSettings from(final SettingValues values) {
    return new GuildSettings(
        values.get(RITE_NEEDS_OATH),
        values.get(RITE_COOLDOWN_SECONDS),
        values.get(RITE_SHIELD_SECONDS));
  }
}
