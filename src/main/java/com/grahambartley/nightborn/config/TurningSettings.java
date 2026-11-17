package com.grahambartley.nightborn.config;

import java.util.List;

public record TurningSettings(
    int biteTurnChance,
    int batTurnChancePerThousand,
    int turningBiteBloodCost,
    boolean playersCanTurnPlayers,
    int sicknessDays,
    int oldBloodRefillDays) {

  static final String SECTION = "turning";

  public static final IntSetting BITE_TURN_CHANCE =
      new IntSetting(SECTION, "biteTurnChance", 100, 0, 100, c -> c.turning().biteTurnChance());
  public static final IntSetting BAT_TURN_CHANCE_PER_THOUSAND =
      new IntSetting(
          SECTION,
          "batTurnChancePerThousand",
          1,
          0,
          1000,
          c -> c.turning().batTurnChancePerThousand());
  public static final IntSetting TURNING_BITE_BLOOD_COST =
      new IntSetting(
          SECTION, "turningBiteBloodCost", 50, 0, 100, c -> c.turning().turningBiteBloodCost());
  public static final BoolSetting PLAYERS_CAN_TURN_PLAYERS =
      new BoolSetting(
          SECTION, "playersCanTurnPlayers", true, c -> c.turning().playersCanTurnPlayers());
  public static final IntSetting SICKNESS_DAYS =
      new IntSetting(SECTION, "sicknessDays", 1, 1, 30, c -> c.turning().sicknessDays());
  public static final IntSetting OLD_BLOOD_REFILL_DAYS =
      new IntSetting(
          SECTION, "oldBloodRefillDays", 7, 1, 60, c -> c.turning().oldBloodRefillDays());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          BITE_TURN_CHANCE,
          BAT_TURN_CHANCE_PER_THOUSAND,
          TURNING_BITE_BLOOD_COST,
          PLAYERS_CAN_TURN_PLAYERS,
          SICKNESS_DAYS,
          OLD_BLOOD_REFILL_DAYS);

  public TurningSettings {
    biteTurnChance = BITE_TURN_CHANCE.clamp(biteTurnChance);
    batTurnChancePerThousand = BAT_TURN_CHANCE_PER_THOUSAND.clamp(batTurnChancePerThousand);
    turningBiteBloodCost = TURNING_BITE_BLOOD_COST.clamp(turningBiteBloodCost);
    sicknessDays = SICKNESS_DAYS.clamp(sicknessDays);
    oldBloodRefillDays = OLD_BLOOD_REFILL_DAYS.clamp(oldBloodRefillDays);
  }

  static TurningSettings from(final SettingValues values) {
    return new TurningSettings(
        values.get(BITE_TURN_CHANCE),
        values.get(BAT_TURN_CHANCE_PER_THOUSAND),
        values.get(TURNING_BITE_BLOOD_COST),
        values.get(PLAYERS_CAN_TURN_PLAYERS),
        values.get(SICKNESS_DAYS),
        values.get(OLD_BLOOD_REFILL_DAYS));
  }
}
