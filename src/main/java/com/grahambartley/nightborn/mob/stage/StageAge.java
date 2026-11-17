package com.grahambartley.nightborn.mob.stage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record StageAge(Text name, Optional<StageDeed> deed) {

  public static final Codec<StageAge> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      TextCodecs.CODEC.fieldOf("name").forGetter(StageAge::name),
                      StageDeed.CODEC.optionalFieldOf("deed").forGetter(StageAge::deed))
                  .apply(instance, StageAge::new));
}
