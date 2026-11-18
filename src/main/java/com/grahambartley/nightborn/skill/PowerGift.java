package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public record PowerGift(Identifier branch, int bloodCut, double cooldownCut) {

  public static final double MOST_COOLDOWN_CUT = 0.8;

  public static final Codec<PowerGift> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("branch").forGetter(PowerGift::branch),
                      Codec.intRange(0, 20)
                          .optionalFieldOf("blood_cut", 0)
                          .forGetter(PowerGift::bloodCut),
                      Codec.doubleRange(0.0, MOST_COOLDOWN_CUT)
                          .optionalFieldOf("cooldown_cut", 0.0)
                          .forGetter(PowerGift::cooldownCut))
                  .apply(instance, PowerGift::new));

  public boolean covers(final Identifier powerBranch) {
    return branch.equals(powerBranch);
  }
}
