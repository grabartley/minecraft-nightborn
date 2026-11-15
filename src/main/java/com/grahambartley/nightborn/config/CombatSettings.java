package com.grahambartley.nightborn.config;

import java.util.List;

public record CombatSettings(
    int leapMinRange,
    int leapMaxRange,
    int leapPush,
    int leapLift,
    int dashMinRange,
    int dashMaxRange,
    int dashPush,
    int dashLift,
    int dodgePush,
    int dodgeLift,
    int swingThreatRange,
    int aimThreatRange,
    int moveCooldownThinks,
    int retreatRegainPercent,
    int retreatLongestSeconds,
    int retreatRestSeconds,
    int retreatDistance,
    int retreatSpeed) {

  static final String SECTION = "combat";

  public static final IntSetting LEAP_MIN_RANGE =
      new IntSetting(SECTION, "leapMinRange", 3, 0, 16, c -> c.combat().leapMinRange());
  public static final IntSetting LEAP_MAX_RANGE =
      new IntSetting(SECTION, "leapMaxRange", 7, 1, 32, c -> c.combat().leapMaxRange());
  public static final IntSetting LEAP_PUSH =
      new IntSetting(SECTION, "leapPush", 90, 0, 300, c -> c.combat().leapPush());
  public static final IntSetting LEAP_LIFT =
      new IntSetting(SECTION, "leapLift", 45, 0, 200, c -> c.combat().leapLift());
  public static final IntSetting DASH_MIN_RANGE =
      new IntSetting(SECTION, "dashMinRange", 7, 0, 32, c -> c.combat().dashMinRange());
  public static final IntSetting DASH_MAX_RANGE =
      new IntSetting(SECTION, "dashMaxRange", 16, 1, 48, c -> c.combat().dashMaxRange());
  public static final IntSetting DASH_PUSH =
      new IntSetting(SECTION, "dashPush", 130, 0, 300, c -> c.combat().dashPush());
  public static final IntSetting DASH_LIFT =
      new IntSetting(SECTION, "dashLift", 12, 0, 200, c -> c.combat().dashLift());
  public static final IntSetting DODGE_PUSH =
      new IntSetting(SECTION, "dodgePush", 85, 0, 300, c -> c.combat().dodgePush());
  public static final IntSetting DODGE_LIFT =
      new IntSetting(SECTION, "dodgeLift", 25, 0, 200, c -> c.combat().dodgeLift());
  public static final IntSetting SWING_THREAT_RANGE =
      new IntSetting(SECTION, "swingThreatRange", 4, 1, 16, c -> c.combat().swingThreatRange());
  public static final IntSetting AIM_THREAT_RANGE =
      new IntSetting(SECTION, "aimThreatRange", 5, 1, 64, c -> c.combat().aimThreatRange());
  public static final IntSetting MOVE_COOLDOWN_THINKS =
      new IntSetting(SECTION, "moveCooldownThinks", 3, 1, 20, c -> c.combat().moveCooldownThinks());
  public static final IntSetting RETREAT_REGAIN_PERCENT =
      new IntSetting(
          SECTION, "retreatRegainPercent", 30, 0, 100, c -> c.combat().retreatRegainPercent());
  public static final IntSetting RETREAT_LONGEST_SECONDS =
      new IntSetting(
          SECTION, "retreatLongestSeconds", 8, 1, 60, c -> c.combat().retreatLongestSeconds());
  public static final IntSetting RETREAT_REST_SECONDS =
      new IntSetting(
          SECTION, "retreatRestSeconds", 15, 0, 300, c -> c.combat().retreatRestSeconds());
  public static final IntSetting RETREAT_DISTANCE =
      new IntSetting(SECTION, "retreatDistance", 14, 4, 48, c -> c.combat().retreatDistance());
  public static final IntSetting RETREAT_SPEED =
      new IntSetting(SECTION, "retreatSpeed", 130, 50, 300, c -> c.combat().retreatSpeed());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          LEAP_MIN_RANGE,
          LEAP_MAX_RANGE,
          LEAP_PUSH,
          LEAP_LIFT,
          DASH_MIN_RANGE,
          DASH_MAX_RANGE,
          DASH_PUSH,
          DASH_LIFT,
          DODGE_PUSH,
          DODGE_LIFT,
          SWING_THREAT_RANGE,
          AIM_THREAT_RANGE,
          MOVE_COOLDOWN_THINKS,
          RETREAT_REGAIN_PERCENT,
          RETREAT_LONGEST_SECONDS,
          RETREAT_REST_SECONDS,
          RETREAT_DISTANCE,
          RETREAT_SPEED);

  public CombatSettings {
    leapMinRange = LEAP_MIN_RANGE.clamp(leapMinRange);
    leapMaxRange = Math.max(leapMinRange, LEAP_MAX_RANGE.clamp(leapMaxRange));
    leapPush = LEAP_PUSH.clamp(leapPush);
    leapLift = LEAP_LIFT.clamp(leapLift);
    dashMinRange = DASH_MIN_RANGE.clamp(dashMinRange);
    dashMaxRange = Math.max(dashMinRange, DASH_MAX_RANGE.clamp(dashMaxRange));
    dashPush = DASH_PUSH.clamp(dashPush);
    dashLift = DASH_LIFT.clamp(dashLift);
    dodgePush = DODGE_PUSH.clamp(dodgePush);
    dodgeLift = DODGE_LIFT.clamp(dodgeLift);
    swingThreatRange = SWING_THREAT_RANGE.clamp(swingThreatRange);
    aimThreatRange = AIM_THREAT_RANGE.clamp(aimThreatRange);
    moveCooldownThinks = MOVE_COOLDOWN_THINKS.clamp(moveCooldownThinks);
    retreatRegainPercent = RETREAT_REGAIN_PERCENT.clamp(retreatRegainPercent);
    retreatLongestSeconds = RETREAT_LONGEST_SECONDS.clamp(retreatLongestSeconds);
    retreatRestSeconds = RETREAT_REST_SECONDS.clamp(retreatRestSeconds);
    retreatDistance = RETREAT_DISTANCE.clamp(retreatDistance);
    retreatSpeed = RETREAT_SPEED.clamp(retreatSpeed);
  }

  static CombatSettings from(final SettingValues values) {
    return new CombatSettings(
        values.get(LEAP_MIN_RANGE),
        values.get(LEAP_MAX_RANGE),
        values.get(LEAP_PUSH),
        values.get(LEAP_LIFT),
        values.get(DASH_MIN_RANGE),
        values.get(DASH_MAX_RANGE),
        values.get(DASH_PUSH),
        values.get(DASH_LIFT),
        values.get(DODGE_PUSH),
        values.get(DODGE_LIFT),
        values.get(SWING_THREAT_RANGE),
        values.get(AIM_THREAT_RANGE),
        values.get(MOVE_COOLDOWN_THINKS),
        values.get(RETREAT_REGAIN_PERCENT),
        values.get(RETREAT_LONGEST_SECONDS),
        values.get(RETREAT_REST_SECONDS),
        values.get(RETREAT_DISTANCE),
        values.get(RETREAT_SPEED));
  }
}
