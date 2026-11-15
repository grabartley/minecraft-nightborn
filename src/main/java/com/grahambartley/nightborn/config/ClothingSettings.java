package com.grahambartley.nightborn.config;

import java.util.List;

public record ClothingSettings(
    boolean nocturne, int batwingBlocks, int glidingStepTenths, int hushSpeed) {

  static final String SECTION = "clothing";

  public static final BoolSetting NOCTURNE =
      new BoolSetting(SECTION, "nocturne", true, c -> c.clothing().nocturne());
  public static final IntSetting BATWING_BLOCKS =
      new IntSetting(SECTION, "batwingBlocks", 1, 0, 5, c -> c.clothing().batwingBlocks());
  public static final IntSetting GLIDING_STEP_TENTHS =
      new IntSetting(SECTION, "glidingStepTenths", 4, 0, 10, c -> c.clothing().glidingStepTenths());
  public static final IntSetting HUSH_SPEED =
      new IntSetting(SECTION, "hushSpeed", 10, 0, 30, c -> c.clothing().hushSpeed());

  public static final List<Setting<?>> SETTINGS =
      List.of(NOCTURNE, BATWING_BLOCKS, GLIDING_STEP_TENTHS, HUSH_SPEED);

  public ClothingSettings {
    batwingBlocks = BATWING_BLOCKS.clamp(batwingBlocks);
    glidingStepTenths = GLIDING_STEP_TENTHS.clamp(glidingStepTenths);
    hushSpeed = HUSH_SPEED.clamp(hushSpeed);
  }

  static ClothingSettings from(final SettingValues values) {
    return new ClothingSettings(
        values.get(NOCTURNE),
        values.get(BATWING_BLOCKS),
        values.get(GLIDING_STEP_TENTHS),
        values.get(HUSH_SPEED));
  }
}
