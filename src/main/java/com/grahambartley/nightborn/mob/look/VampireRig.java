package com.grahambartley.nightborn.mob.look;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.clothing.ClothingCut;
import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.MathHelper;

public enum VampireRig implements StringIdentifiable {
  MALE("male", ClothingCut.MALE, Figure.FLAT, Figure.FLAT, "vampire", false),
  FEMALE("female", ClothingCut.FEMALE, 0.7F, 1.3F, "vampire", false),
  LORD("lord", ClothingCut.MALE, Figure.FLAT, Figure.FLAT, "vampire_lord", false),
  ELDEST_MALE("eldest_male", ClothingCut.MALE, Figure.FLAT, Figure.FLAT, "vampire_eldest", true);

  public static final Codec<VampireRig> CODEC = StringIdentifiable.createCodec(VampireRig::values);

  public static final Set<VampireRig> COMMON =
      Collections.unmodifiableSet(EnumSet.of(MALE, FEMALE));

  public static final Set<VampireRig> ELDEST = Collections.unmodifiableSet(EnumSet.of(ELDEST_MALE));

  private final String id;
  private final ClothingCut cut;
  private final float smallestBust;
  private final float largestBust;
  private final String clips;
  private final boolean rages;

  VampireRig(
      final String id,
      final ClothingCut cut,
      final float smallestBust,
      final float largestBust,
      final String clips,
      final boolean rages) {
    this.id = id;
    this.cut = cut;
    this.smallestBust = smallestBust;
    this.largestBust = largestBust;
    this.clips = clips;
    this.rages = rages;
  }

  public float bust(final double roll) {
    return MathHelper.lerp((float) MathHelper.clamp(roll, 0.0, 1.0), smallestBust, largestBust);
  }

  public float middleBust() {
    return bust(0.5);
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

  public Identifier eyes(final boolean blazing) {
    return blazing && rages
        ? Identifier.of(Nightborn.MOD_ID, "textures/entity/vampire/" + id + "_eyes_blazing.png")
        : eyes();
  }

  public static Identifier playerClips() {
    return MALE.clips();
  }

  public Identifier clips() {
    return Identifier.of(Nightborn.MOD_ID, "animations/entity/" + clips + ".animation.json");
  }

  @Override
  public String asString() {
    return id;
  }
}
