package com.grahambartley.nightborn.config;

import java.util.List;

public record FactionSettings(
    boolean showInPlayerList, int ceremonySeconds, int titleCardSeconds, int ceremonyRange) {

  static final String SECTION = "factions";

  public static final BoolSetting SHOW_IN_PLAYER_LIST =
      new BoolSetting(SECTION, "showInPlayerList", false, c -> c.factions().showInPlayerList());
  public static final IntSetting CEREMONY_SECONDS =
      new IntSetting(SECTION, "ceremonySeconds", 5, 1, 20, c -> c.factions().ceremonySeconds());
  public static final IntSetting TITLE_CARD_SECONDS =
      new IntSetting(SECTION, "titleCardSeconds", 3, 1, 10, c -> c.factions().titleCardSeconds());
  public static final IntSetting CEREMONY_RANGE =
      new IntSetting(SECTION, "ceremonyRange", 32, 0, 128, c -> c.factions().ceremonyRange());

  public static final List<Setting<?>> SETTINGS =
      List.of(SHOW_IN_PLAYER_LIST, CEREMONY_SECONDS, TITLE_CARD_SECONDS, CEREMONY_RANGE);

  public FactionSettings {
    ceremonySeconds = CEREMONY_SECONDS.clamp(ceremonySeconds);
    titleCardSeconds = TITLE_CARD_SECONDS.clamp(titleCardSeconds);
    ceremonyRange = CEREMONY_RANGE.clamp(ceremonyRange);
  }

  static FactionSettings from(final SettingValues values) {
    return new FactionSettings(
        values.get(SHOW_IN_PLAYER_LIST),
        values.get(CEREMONY_SECONDS),
        values.get(TITLE_CARD_SECONDS),
        values.get(CEREMONY_RANGE));
  }
}
