package com.grahambartley.nightborn.advancement;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringIdentifiable;

public enum Milestone implements StringIdentifiable {
  BITTEN,
  TURNED,
  FED,
  FED_WATCHED,
  SUN_SURVIVED,
  PARASOL_SHADE,
  BAT_FORM,
  GARLIC_STRINGS,
  DAY_SKIPPED,
  BLOODLINES,
  FAMILIAR_HIRED,
  VAMPIRE_STAKED,
  HOLY_WATER_HIT,
  SICKNESS_CURED,
  STRUCTURE_ENTERED,
  SWORN_IN;

  public static final Codec<Milestone> CODEC = StringIdentifiable.createCodec(Milestone::values);

  @Override
  public String asString() {
    return name().toLowerCase(Locale.ROOT);
  }
}
