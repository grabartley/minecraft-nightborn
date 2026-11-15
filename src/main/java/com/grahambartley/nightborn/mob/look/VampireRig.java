package com.grahambartley.nightborn.mob.look;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.clothing.ClothingCut;
import com.mojang.serialization.Codec;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public enum VampireRig implements StringIdentifiable {
  MALE("male", ClothingCut.MALE),
  FEMALE("female", ClothingCut.FEMALE);

  public static final Codec<VampireRig> CODEC = StringIdentifiable.createCodec(VampireRig::values);

  private final String id;
  private final ClothingCut cut;

  VampireRig(final String id, final ClothingCut cut) {
    this.id = id;
    this.cut = cut;
  }

  public ClothingCut cut() {
    return cut;
  }

  public Identifier geo() {
    return Identifier.of(Nightborn.MOD_ID, "geo/entity/vampire_" + id + ".geo.json");
  }

  public Identifier eyes() {
    return Identifier.of(Nightborn.MOD_ID, "textures/entity/vampire/" + id + "_eyes.png");
  }

  public static Identifier animation() {
    return Identifier.of(Nightborn.MOD_ID, "animations/entity/vampire.animation.json");
  }

  @Override
  public String asString() {
    return id;
  }
}
