package com.grahambartley.nightborn.config;

import java.util.List;

public record DownedSettings(
    int downedSeconds, int escapeSeconds, int recoverHealth, boolean hardcoreVampireDeath) {

  static final String SECTION = "downed";

  public static final IntSetting DOWNED_SECONDS =
      new IntSetting(SECTION, "downedSeconds", 30, 5, 600, c -> c.downed().downedSeconds());
  public static final IntSetting ESCAPE_SECONDS =
      new IntSetting(SECTION, "escapeSeconds", 60, 5, 600, c -> c.downed().escapeSeconds());
  public static final IntSetting RECOVER_HEALTH =
      new IntSetting(SECTION, "recoverHealth", 20, 1, 100, c -> c.downed().recoverHealth());
  public static final BoolSetting HARDCORE_VAMPIRE_DEATH =
      new BoolSetting(
          SECTION, "hardcoreVampireDeath", false, c -> c.downed().hardcoreVampireDeath());

  public static final List<Setting<?>> SETTINGS =
      List.of(DOWNED_SECONDS, ESCAPE_SECONDS, RECOVER_HEALTH, HARDCORE_VAMPIRE_DEATH);

  public DownedSettings {
    downedSeconds = DOWNED_SECONDS.clamp(downedSeconds);
    escapeSeconds = ESCAPE_SECONDS.clamp(escapeSeconds);
    recoverHealth = RECOVER_HEALTH.clamp(recoverHealth);
  }

  static DownedSettings from(final SettingValues values) {
    return new DownedSettings(
        values.get(DOWNED_SECONDS),
        values.get(ESCAPE_SECONDS),
        values.get(RECOVER_HEALTH),
        values.get(HARDCORE_VAMPIRE_DEATH));
  }
}
