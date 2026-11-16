package com.grahambartley.nightborn.mob.look;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.clothing.ClothingCut;
import com.mojang.serialization.Codec;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.MathHelper;

public enum VampireRig implements StringIdentifiable {
  MALE("male", ClothingCut.MALE, Figure.FLAT, Figure.FLAT),
  FEMALE("female", ClothingCut.FEMALE, 0.7F, 1.3F);

  public static final Codec<VampireRig> CODEC = StringIdentifiable.createCodec(VampireRig::values);

  private final String id;
  private final ClothingCut cut;
  private final float smallestBust;
  private final float largestBust;

  VampireRig(
      final String id, final ClothingCut cut, final float smallestBust, final float largestBust) {
    this.id = id;
    this.cut = cut;
    this.smallestBust = smallestBust;
    this.largestBust = largestBust;
  }

  public float bust(final double roll) {
    return MathHelper.lerp((float) MathHelper.clamp(roll, 0.0, 1.0), smallestBust, largestBust);
  }

  public float fitBust(final float bust) {
    return MathHelper.clamp(bust, smallestBust, largestBust);
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
