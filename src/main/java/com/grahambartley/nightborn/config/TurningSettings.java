package com.grahambartley.nightborn.config;

import java.util.List;

public record TurningSettings(
    int biteTurnChance,
    int vampireMobTurnChance,
    int vampireBatTurnChance,
    boolean playersCanTurnPlayers,
    int sicknessDays) {

  static final String SECTION = "turning";

  public static final IntSetting BITE_TURN_CHANCE =
      new IntSetting(SECTION, "biteTurnChance", 100, 0, 100, c -> c.turning().biteTurnChance());
  public static final IntSetting VAMPIRE_MOB_TURN_CHANCE =
      new IntSetting(
          SECTION, "vampireMobTurnChance", 25, 0, 100, c -> c.turning().vampireMobTurnChance());
  public static final IntSetting VAMPIRE_BAT_TURN_CHANCE =
      new IntSetting(
          SECTION, "vampireBatTurnChance", 10, 0, 100, c -> c.turning().vampireBatTurnChance());
  public static final BoolSetting PLAYERS_CAN_TURN_PLAYERS =
      new BoolSetting(
          SECTION, "playersCanTurnPlayers", true, c -> c.turning().playersCanTurnPlayers());
  public static final IntSetting SICKNESS_DAYS =
      new IntSetting(SECTION, "sicknessDays", 3, 1, 30, c -> c.turning().sicknessDays());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          BITE_TURN_CHANCE,
          VAMPIRE_MOB_TURN_CHANCE,
          VAMPIRE_BAT_TURN_CHANCE,
          PLAYERS_CAN_TURN_PLAYERS,
          SICKNESS_DAYS);

  public TurningSettings {
    biteTurnChance = BITE_TURN_CHANCE.clamp(biteTurnChance);
    vampireMobTurnChance = VAMPIRE_MOB_TURN_CHANCE.clamp(vampireMobTurnChance);
    vampireBatTurnChance = VAMPIRE_BAT_TURN_CHANCE.clamp(vampireBatTurnChance);
    sicknessDays = SICKNESS_DAYS.clamp(sicknessDays);
  }

  static TurningSettings from(final SettingValues values) {
    return new TurningSettings(
        values.get(BITE_TURN_CHANCE),
        values.get(VAMPIRE_MOB_TURN_CHANCE),
        values.get(VAMPIRE_BAT_TURN_CHANCE),
        values.get(PLAYERS_CAN_TURN_PLAYERS),
        values.get(SICKNESS_DAYS));
  }
}
