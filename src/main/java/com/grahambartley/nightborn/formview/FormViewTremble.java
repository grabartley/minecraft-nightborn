package com.grahambartley.nightborn.formview;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FormViewTremble(float amount, float inner, float speed) {

  public static final Codec<FormViewTremble> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(0.0F, 0.05F)
                          .fieldOf("amount")
                          .forGetter(FormViewTremble::amount),
                      Codec.floatRange(0.0F, 1.0F)
                          .fieldOf("inner")
                          .forGetter(FormViewTremble::inner),
                      Codec.floatRange(0.0F, 60.0F)
                          .fieldOf("speed")
                          .forGetter(FormViewTremble::speed))
                  .apply(instance, FormViewTremble::new));
}
