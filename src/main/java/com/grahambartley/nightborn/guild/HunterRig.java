package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.mob.look.VampireRig;
import com.mojang.serialization.Codec;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public enum HunterRig implements StringIdentifiable {
  MALE("male", "hunter_male", VampireRig.MALE),
  FEMALE("female", "hunter_female", VampireRig.FEMALE),
  GUILD_MASTER("guild_master", "guild_master", VampireRig.MALE),
  VETERAN_MALE("veteran_male", "hunter_male", VampireRig.MALE),
  VETERAN_FEMALE("veteran_female", "hunter_female", VampireRig.FEMALE);

  public static final Codec<HunterRig> CODEC = StringIdentifiable.createCodec(HunterRig::values);

  public static final Identifier ANIMATION =
      Identifier.of(Nightborn.MOD_ID, "animations/entity/hunter.animation.json");

  private final String id;
  private final Identifier geo;
  private final VampireRig figure;

  HunterRig(final String id, final String geo, final VampireRig figure) {
    this.id = id;
    this.geo = Identifier.of(Nightborn.MOD_ID, "geo/entity/" + geo + ".geo.json");
    this.figure = figure;
  }

  public Identifier geo() {
    return geo;
  }

  public VampireRig figure() {
    return figure;
  }

  public static HunterRig hunter(final boolean female) {
    return female ? FEMALE : MALE;
  }

  public static HunterRig veteran(final boolean female) {
    return female ? VETERAN_FEMALE : VETERAN_MALE;
  }

  @Override
  public String asString() {
    return id;
  }
}
