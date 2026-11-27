package com.grahambartley.nightborn.config;

import java.util.List;

public record GuildSettings(
    boolean riteNeedsOath,
    int riteMinRank,
    int ritePrice,
    int riteCooldownSeconds,
    int riteShieldSeconds) {

  static final String SECTION = "guild";

  public static final BoolSetting RITE_NEEDS_OATH =
      new BoolSetting(SECTION, "riteNeedsOath", true, c -> c.guild().riteNeedsOath());
  public static final IntSetting RITE_MIN_RANK =
      new IntSetting(SECTION, "riteMinRank", 1, 0, 3, c -> c.guild().riteMinRank());
  public static final IntSetting RITE_PRICE =
      new IntSetting(SECTION, "ritePrice", 16, 0, 64, c -> c.guild().ritePrice());
  public static final IntSetting RITE_COOLDOWN_SECONDS =
      new IntSetting(
          SECTION, "riteCooldownSeconds", 1200, 0, 7200, c -> c.guild().riteCooldownSeconds());
  public static final IntSetting RITE_SHIELD_SECONDS =
      new IntSetting(SECTION, "riteShieldSeconds", 3, 0, 30, c -> c.guild().riteShieldSeconds());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          RITE_NEEDS_OATH, RITE_MIN_RANK, RITE_PRICE, RITE_COOLDOWN_SECONDS, RITE_SHIELD_SECONDS);

  public GuildSettings {
    riteMinRank = RITE_MIN_RANK.clamp(riteMinRank);
    ritePrice = RITE_PRICE.clamp(ritePrice);
    riteCooldownSeconds = RITE_COOLDOWN_SECONDS.clamp(riteCooldownSeconds);
    riteShieldSeconds = RITE_SHIELD_SECONDS.clamp(riteShieldSeconds);
  }

  static GuildSettings from(final SettingValues values) {
    return new GuildSettings(
        values.get(RITE_NEEDS_OATH),
        values.get(RITE_MIN_RANK),
        values.get(RITE_PRICE),
        values.get(RITE_COOLDOWN_SECONDS),
        values.get(RITE_SHIELD_SECONDS));
  }
}
