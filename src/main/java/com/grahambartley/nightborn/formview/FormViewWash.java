package com.grahambartley.nightborn.formview;

import com.grahambartley.nightborn.mob.look.HexColor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FormViewWash(int color, float amount, float saturation, float blur, float drift) {

  public static final FormViewWash NONE = new FormViewWash(0, 0.0F, 1.0F, 0.0F, 0.0F);

  private static final Codec<Float> AMOUNT = Codec.floatRange(0.0F, 1.0F);

  public static final Codec<FormViewWash> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      HexColor.CODEC.fieldOf("color").forGetter(FormViewWash::color),
                      AMOUNT.fieldOf("amount").forGetter(FormViewWash::amount),
                      AMOUNT.fieldOf("saturation").forGetter(FormViewWash::saturation),
                      Codec.floatRange(0.0F, 8.0F).fieldOf("blur").forGetter(FormViewWash::blur),
                      Codec.floatRange(0.0F, 0.02F).fieldOf("drift").forGetter(FormViewWash::drift))
                  .apply(instance, FormViewWash::new));
}
