package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public record BoughtSkill(Identifier skill, int paid) {

  public static final Codec<BoughtSkill> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("skill").forGetter(BoughtSkill::skill),
                      Codec.intRange(0, Integer.MAX_VALUE)
                          .fieldOf("paid")
                          .forGetter(BoughtSkill::paid))
                  .apply(instance, BoughtSkill::new));
}
