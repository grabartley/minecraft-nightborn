package com.grahambartley.nightborn.mob.stage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;

public record StageSkills(IntProvider count, Map<Identifier, Integer> weights, int otherWeight) {

  public static final int MOST_SKILLS = 64;

  public static final StageSkills NONE =
      new StageSkills(ConstantIntProvider.create(0), Map.of(), 1);

  public static final Codec<StageSkills> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      IntProvider.createValidatingCodec(0, MOST_SKILLS)
                          .fieldOf("count")
                          .forGetter(StageSkills::count),
                      Codec.unboundedMap(Identifier.CODEC, Codec.intRange(0, StageOdds.HEAVIEST))
                          .optionalFieldOf("weights", Map.of())
                          .forGetter(StageSkills::weights),
                      Codec.intRange(0, StageOdds.HEAVIEST)
                          .optionalFieldOf("other_weight", 1)
                          .forGetter(StageSkills::otherWeight))
                  .apply(instance, StageSkills::new));

  public StageSkills {
    weights = Map.copyOf(weights);
  }

  public int weight(final Identifier skill) {
    return weights.getOrDefault(skill, otherWeight);
  }
}
