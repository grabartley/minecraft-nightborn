package com.grahambartley.nightborn.config;

import java.util.List;

public record ClothingSettings(
    boolean nocturne,
    int batwingBlocks,
    int glidingStepTenths,
    int hushSpeed,
    int shroudPercent,
    int mistcloakBlocks,
    int wardingPercent,
    int thriftPercent) {

  static final String SECTION = "clothing";

  public static final BoolSetting NOCTURNE =
      new BoolSetting(SECTION, "nocturne", true, c -> c.clothing().nocturne());
  public static final IntSetting BATWING_BLOCKS =
      new IntSetting(SECTION, "batwingBlocks", 1, 0, 5, c -> c.clothing().batwingBlocks());
  public static final IntSetting GLIDING_STEP_TENTHS =
      new IntSetting(SECTION, "glidingStepTenths", 4, 0, 10, c -> c.clothing().glidingStepTenths());
  public static final IntSetting HUSH_SPEED =
      new IntSetting(SECTION, "hushSpeed", 10, 0, 30, c -> c.clothing().hushSpeed());
  public static final IntSetting SHROUD_PERCENT =
      new IntSetting(SECTION, "shroudPercent", 10, 0, 25, c -> c.clothing().shroudPercent());
  public static final IntSetting MISTCLOAK_BLOCKS =
      new IntSetting(SECTION, "mistcloakBlocks", 1, 0, 3, c -> c.clothing().mistcloakBlocks());
  public static final IntSetting WARDING_PERCENT =
      new IntSetting(SECTION, "wardingPercent", 10, 0, 25, c -> c.clothing().wardingPercent());
  public static final IntSetting THRIFT_PERCENT =
      new IntSetting(SECTION, "thriftPercent", 5, 0, 25, c -> c.clothing().thriftPercent());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          NOCTURNE,
          BATWING_BLOCKS,
          GLIDING_STEP_TENTHS,
          HUSH_SPEED,
          SHROUD_PERCENT,
          MISTCLOAK_BLOCKS,
          WARDING_PERCENT,
          THRIFT_PERCENT);

  public ClothingSettings {
    batwingBlocks = BATWING_BLOCKS.clamp(batwingBlocks);
    glidingStepTenths = GLIDING_STEP_TENTHS.clamp(glidingStepTenths);
    hushSpeed = HUSH_SPEED.clamp(hushSpeed);
    shroudPercent = SHROUD_PERCENT.clamp(shroudPercent);
    mistcloakBlocks = MISTCLOAK_BLOCKS.clamp(mistcloakBlocks);
    wardingPercent = WARDING_PERCENT.clamp(wardingPercent);
    thriftPercent = THRIFT_PERCENT.clamp(thriftPercent);
  }

  static ClothingSettings from(final SettingValues values) {
    return new ClothingSettings(
        values.get(NOCTURNE),
        values.get(BATWING_BLOCKS),
        values.get(GLIDING_STEP_TENTHS),
        values.get(HUSH_SPEED),
        values.get(SHROUD_PERCENT),
        values.get(MISTCLOAK_BLOCKS),
        values.get(WARDING_PERCENT),
        values.get(THRIFT_PERCENT));
  }
}
