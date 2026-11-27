package com.grahambartley.nightborn.formview;

import com.grahambartley.nightborn.mob.look.HexColor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FormViewTint(int color, float centre, float edge) {

  public static final FormViewTint NONE = new FormViewTint(0, 0.0F, 0.0F);

  private static final Codec<Float> AMOUNT = Codec.floatRange(0.0F, 1.0F);

  public static final Codec<FormViewTint> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      HexColor.CODEC.fieldOf("color").forGetter(FormViewTint::color),
                      AMOUNT.fieldOf("centre").forGetter(FormViewTint::centre),
                      AMOUNT.fieldOf("edge").forGetter(FormViewTint::edge))
                  .apply(instance, FormViewTint::new));
}
