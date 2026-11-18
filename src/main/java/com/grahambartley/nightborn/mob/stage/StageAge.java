package com.grahambartley.nightborn.mob.stage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record StageAge(Text name, Optional<StageDeed> deed, double notoriety) {

  public static final double PLAIN_NOTORIETY = 1.0;

  public static final Codec<StageAge> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      TextCodecs.CODEC.fieldOf("name").forGetter(StageAge::name),
                      StageDeed.CODEC.optionalFieldOf("deed").forGetter(StageAge::deed),
                      Codec.doubleRange(0.0, Double.MAX_VALUE)
                          .optionalFieldOf("notoriety", PLAIN_NOTORIETY)
                          .forGetter(StageAge::notoriety))
                  .apply(instance, StageAge::new));
}
