package com.grahambartley.nightborn.config;

import java.util.List;

public record RitualSettings(
    int buildUpTicks,
    int eldestPoints,
    int eldestHealthPerPlayer,
    int fightRange,
    int arenaRadius,
    int roomArenaRadius,
    int arenaEmptyTicks,
    int stakeWindowSeconds,
    int riseHealth,
    int bladeLifeSteal,
    int setLifeSteal,
    int setHealth) {

  static final String SECTION = "ritual";

  public static final IntSetting BUILD_UP_TICKS =
      new IntSetting(SECTION, "buildUpTicks", 200, 20, 1200, c -> c.ritual().buildUpTicks());
  public static final IntSetting ELDEST_POINTS =
      new IntSetting(SECTION, "eldestPoints", 40, 0, 500, c -> c.ritual().eldestPoints());
  public static final IntSetting ELDEST_HEALTH_PER_PLAYER =
      new IntSetting(
          SECTION, "eldestHealthPerPlayer", 50, 0, 500, c -> c.ritual().eldestHealthPerPlayer());
  public static final IntSetting FIGHT_RANGE =
      new IntSetting(SECTION, "fightRange", 32, 8, 128, c -> c.ritual().fightRange());
  public static final IntSetting ARENA_RADIUS =
      new IntSetting(SECTION, "arenaRadius", 16, 8, 48, c -> c.ritual().arenaRadius());
  public static final IntSetting ROOM_ARENA_RADIUS =
      new IntSetting(SECTION, "roomArenaRadius", 10, 4, 32, c -> c.ritual().roomArenaRadius());
  public static final IntSetting ARENA_EMPTY_TICKS =
      new IntSetting(SECTION, "arenaEmptyTicks", 100, 20, 1200, c -> c.ritual().arenaEmptyTicks());
  public static final IntSetting STAKE_WINDOW_SECONDS =
      new IntSetting(
          SECTION, "stakeWindowSeconds", 15, 5, 120, c -> c.ritual().stakeWindowSeconds());
  public static final IntSetting RISE_HEALTH =
      new IntSetting(SECTION, "riseHealth", 30, 1, 100, c -> c.ritual().riseHealth());
  public static final IntSetting BLADE_LIFE_STEAL =
      new IntSetting(SECTION, "bladeLifeSteal", 10, 0, 100, c -> c.ritual().bladeLifeSteal());
  public static final IntSetting SET_LIFE_STEAL =
      new IntSetting(SECTION, "setLifeSteal", 20, 0, 100, c -> c.ritual().setLifeSteal());
  public static final IntSetting SET_HEALTH =
      new IntSetting(SECTION, "setHealth", 4, 0, 20, c -> c.ritual().setHealth());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          BUILD_UP_TICKS,
          ELDEST_POINTS,
          ELDEST_HEALTH_PER_PLAYER,
          FIGHT_RANGE,
          ARENA_RADIUS,
          ROOM_ARENA_RADIUS,
          ARENA_EMPTY_TICKS,
          STAKE_WINDOW_SECONDS,
          RISE_HEALTH,
          BLADE_LIFE_STEAL,
          SET_LIFE_STEAL,
          SET_HEALTH);

  public RitualSettings {
    buildUpTicks = BUILD_UP_TICKS.clamp(buildUpTicks);
    eldestPoints = ELDEST_POINTS.clamp(eldestPoints);
    eldestHealthPerPlayer = ELDEST_HEALTH_PER_PLAYER.clamp(eldestHealthPerPlayer);
    fightRange = FIGHT_RANGE.clamp(fightRange);
    arenaRadius = ARENA_RADIUS.clamp(arenaRadius);
    roomArenaRadius = ROOM_ARENA_RADIUS.clamp(roomArenaRadius);
    arenaEmptyTicks = ARENA_EMPTY_TICKS.clamp(arenaEmptyTicks);
    stakeWindowSeconds = STAKE_WINDOW_SECONDS.clamp(stakeWindowSeconds);
    riseHealth = RISE_HEALTH.clamp(riseHealth);
    bladeLifeSteal = BLADE_LIFE_STEAL.clamp(bladeLifeSteal);
    setLifeSteal = SET_LIFE_STEAL.clamp(setLifeSteal);
    setHealth = SET_HEALTH.clamp(setHealth);
  }

  static RitualSettings from(final SettingValues values) {
    return new RitualSettings(
        values.get(BUILD_UP_TICKS),
        values.get(ELDEST_POINTS),
        values.get(ELDEST_HEALTH_PER_PLAYER),
        values.get(FIGHT_RANGE),
        values.get(ARENA_RADIUS),
        values.get(ROOM_ARENA_RADIUS),
        values.get(ARENA_EMPTY_TICKS),
        values.get(STAKE_WINDOW_SECONDS),
        values.get(RISE_HEALTH),
        values.get(BLADE_LIFE_STEAL),
        values.get(SET_LIFE_STEAL),
        values.get(SET_HEALTH));
  }
}
