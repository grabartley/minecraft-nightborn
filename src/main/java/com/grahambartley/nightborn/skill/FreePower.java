package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.Identifier;

public record FreePower(List<Identifier> skills, int durationTicks) {

  public static final Codec<FreePower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.listOf().fieldOf("skills").forGetter(FreePower::skills),
                      Codec.intRange(1, 6000)
                          .fieldOf("duration_ticks")
                          .forGetter(FreePower::durationTicks))
                  .apply(instance, FreePower::new));

  public FreePower {
    skills = List.copyOf(skills);
  }
}
