package com.grahambartley.nightborn.guild.build;

import com.grahambartley.nightborn.mob.look.Weighted;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;

public record HunterBuild(
    Map<Identifier, Integer> rankWeights,
    Map<Identifier, Integer> thinkTicks,
    int topRankSpread,
    int coreWeight,
    int pathWeight,
    Map<Identifier, Map<Identifier, Integer>> kitLeanings,
    int trickleExperience,
    int trickleTicks,
    int stakeExperience) {

  public static final int MOST_THINK_TICKS = 400;
  public static final int MOST_SPREAD = 100;
  public static final int MOST_EXPERIENCE = 100_000;
  public static final int MOST_TRICKLE_TICKS = 72_000;
  public static final int PLAIN_THINK_TICKS = 60;

  public static final HunterBuild NONE =
      new HunterBuild(Map.of(), Map.of(), 0, 1, 1, Map.of(), 0, 1200, 0);

  private static final Codec<Integer> WEIGHT = Codec.intRange(0, Weighted.MAX_WEIGHT);

  public static final Codec<HunterBuild> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.unboundedMap(Identifier.CODEC, WEIGHT)
                          .fieldOf("rank_weights")
                          .forGetter(HunterBuild::rankWeights),
                      Codec.unboundedMap(Identifier.CODEC, Codec.intRange(1, MOST_THINK_TICKS))
                          .optionalFieldOf("think_ticks", Map.of())
                          .forGetter(HunterBuild::thinkTicks),
                      Codec.intRange(0, MOST_SPREAD)
                          .optionalFieldOf("top_rank_spread", 0)
                          .forGetter(HunterBuild::topRankSpread),
                      WEIGHT.optionalFieldOf("core_weight", 1).forGetter(HunterBuild::coreWeight),
                      WEIGHT.optionalFieldOf("path_weight", 1).forGetter(HunterBuild::pathWeight),
                      Codec.unboundedMap(
                              Identifier.CODEC, Codec.unboundedMap(Identifier.CODEC, WEIGHT))
                          .optionalFieldOf("kit_leanings", Map.of())
                          .forGetter(HunterBuild::kitLeanings),
                      Codec.intRange(0, MOST_EXPERIENCE)
                          .optionalFieldOf("trickle_experience", 0)
                          .forGetter(HunterBuild::trickleExperience),
                      Codec.intRange(1, MOST_TRICKLE_TICKS)
                          .optionalFieldOf("trickle_ticks", 1200)
                          .forGetter(HunterBuild::trickleTicks),
                      Codec.intRange(0, MOST_EXPERIENCE)
                          .optionalFieldOf("stake_experience", 0)
                          .forGetter(HunterBuild::stakeExperience))
                  .apply(instance, HunterBuild::new));

  public HunterBuild {
    rankWeights = Map.copyOf(rankWeights);
    thinkTicks = Map.copyOf(thinkTicks);
    kitLeanings = Map.copyOf(kitLeanings);
  }

  public int thinkTicks(final Optional<Identifier> rank) {
    return rank.map(id -> thinkTicks.getOrDefault(id, PLAIN_THINK_TICKS)).orElse(PLAIN_THINK_TICKS);
  }

  public int weight(final Optional<Identifier> kit, final Identifier skill, final boolean path) {
    final int leaning =
        kit.map(id -> kitLeanings.getOrDefault(id, Map.of()).getOrDefault(skill, 0)).orElse(0);
    return (path ? pathWeight : coreWeight) + leaning;
  }

  public boolean trickles(final long age) {
    return trickleExperience > 0 && age > 0 && age % trickleTicks == 0;
  }
}
