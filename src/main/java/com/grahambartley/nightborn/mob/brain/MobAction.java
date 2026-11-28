package com.grahambartley.nightborn.mob.brain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record MobAction(List<MobUse> uses) {

  public static final Codec<MobAction> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(MobUse.CODEC.listOf().fieldOf("uses").forGetter(MobAction::uses))
                  .apply(instance, MobAction::new));

  public MobAction {
    uses = List.copyOf(uses);
  }
}
