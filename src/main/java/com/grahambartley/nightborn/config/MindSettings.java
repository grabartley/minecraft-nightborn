package com.grahambartley.nightborn.config;

import java.util.List;

public record MindSettings(int thrallChance, int charmSeconds) {

  static final String SECTION = "minds";

  public static final IntSetting THRALL_CHANCE =
      new IntSetting(SECTION, "thrallChance", 35, 0, 100, c -> c.minds().thrallChance());
  public static final IntSetting CHARM_SECONDS =
      new IntSetting(SECTION, "charmSeconds", 300, 10, 3600, c -> c.minds().charmSeconds());

  public static final List<Setting<?>> SETTINGS = List.of(THRALL_CHANCE, CHARM_SECONDS);

  public MindSettings {
    thrallChance = THRALL_CHANCE.clamp(thrallChance);
    charmSeconds = CHARM_SECONDS.clamp(charmSeconds);
  }

  static MindSettings from(final SettingValues values) {
    return new MindSettings(values.get(THRALL_CHANCE), values.get(CHARM_SECONDS));
  }
}
