package com.grahambartley.nightborn.servant.job;

import com.grahambartley.nightborn.wheel.WheelEntry;
import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public enum DayJob implements StringIdentifiable {
  GATHER("gather"),
  FETCH_BLOOD("fetch_blood"),
  SCOUT("scout"),
  STAND_WATCH("stand_watch");

  public static final Codec<DayJob> CODEC = StringIdentifiable.createCodec(DayJob::values);

  private final Identifier icon;

  DayJob(final String icon) {
    this.icon = WheelEntry.icon(icon);
  }

  public Identifier icon() {
    return icon;
  }

  public String translationKey() {
    return "nightborn.job." + asString();
  }

  public String orderedKey() {
    return translationKey() + ".ordered";
  }

  public static DayJob byIndex(final int index) {
    return index >= 0 && index < values().length ? values()[index] : GATHER;
  }

  @Override
  public String asString() {
    return name().toLowerCase(Locale.ROOT);
  }
}
