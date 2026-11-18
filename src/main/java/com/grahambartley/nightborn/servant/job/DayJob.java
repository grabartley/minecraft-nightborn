package com.grahambartley.nightborn.servant.job;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public enum DayJob implements StringIdentifiable {
  GATHER("minecraft:iron_hoe"),
  SCOUT("minecraft:spyglass"),
  STAND_WATCH("minecraft:bell");

  public static final Codec<DayJob> CODEC = StringIdentifiable.createCodec(DayJob::values);

  private final Identifier icon;

  DayJob(final String icon) {
    this.icon = Identifier.of(icon);
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
