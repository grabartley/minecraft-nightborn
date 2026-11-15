package com.grahambartley.nightborn.config;

import java.util.List;

public record FactionSettings(boolean showInPlayerList) {

  static final String SECTION = "factions";

  public static final BoolSetting SHOW_IN_PLAYER_LIST =
      new BoolSetting(SECTION, "showInPlayerList", false, c -> c.factions().showInPlayerList());

  public static final List<Setting<?>> SETTINGS = List.of(SHOW_IN_PLAYER_LIST);

  static FactionSettings from(final SettingValues values) {
    return new FactionSettings(values.get(SHOW_IN_PLAYER_LIST));
  }
}
