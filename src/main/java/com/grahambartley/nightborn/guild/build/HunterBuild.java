package com.grahambartley.nightborn.guild.build;

import com.grahambartley.nightborn.mob.brain.Disposition;
import com.grahambartley.nightborn.mob.brain.Mind;
import com.grahambartley.nightborn.mob.brain.Sharpness;
import com.grahambartley.nightborn.mob.look.Weighted;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.util.Identifier;

public record HunterBuild(
    Map<Identifier, Integer> rankWeights,
    Map<Identifier, Integer> thinkTicks,
    Map<Identifier, Sharpness> sharpness,
    int topRankSpread,
    int coreWeight,
    int pathWeight,
    Map<Identifier, Map<Identifier, Integer>> kitLeanings,
    Map<Identifier, Disposition> kitDispositions,
    int trickleExperience,
    int trickleTicks,
    int stakeExperience,
    Map<Identifier, Integer> masterKits) {

  public static final int MOST_THINK_TICKS = 400;
  public static final int MOST_SPREAD = 100;
  public static final int MOST_EXPERIENCE = 100_000;
  public static final int MOST_TRICKLE_TICKS = 72_000;
  public static final int PLAIN_THINK_TICKS = 60;
  public static final int PLAIN_TRICKLE_TICKS = 1200;

  public static final HunterBuild NONE =
      new HunterBuild(
          Map.of(),
          Map.of(),
          Map.of(),
          0,
          1,
          1,
          Map.of(),
          Map.of(),
          0,
          PLAIN_TRICKLE_TICKS,
          0,
          Map.of());

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
                      Codec.unboundedMap(Identifier.CODEC, Sharpness.CODEC)
                          .optionalFieldOf("sharpness", Map.of())
                          .forGetter(HunterBuild::sharpness),
                      Codec.intRange(0, MOST_SPREAD)
                          .optionalFieldOf("top_rank_spread", 0)
                          .forGetter(HunterBuild::topRankSpread),
                      WEIGHT.optionalFieldOf("core_weight", 1).forGetter(HunterBuild::coreWeight),
                      WEIGHT.optionalFieldOf("path_weight", 1).forGetter(HunterBuild::pathWeight),
                      Codec.unboundedMap(
                              Identifier.CODEC, Codec.unboundedMap(Identifier.CODEC, WEIGHT))
                          .optionalFieldOf("kit_leanings", Map.of())
                          .forGetter(HunterBuild::kitLeanings),
                      Codec.unboundedMap(Identifier.CODEC, Disposition.CODEC)
                          .optionalFieldOf("kit_dispositions", Map.of())
                          .forGetter(HunterBuild::kitDispositions),
                      Codec.intRange(0, MOST_EXPERIENCE)
                          .optionalFieldOf("trickle_experience", 0)
                          .forGetter(HunterBuild::trickleExperience),
                      Codec.intRange(1, MOST_TRICKLE_TICKS)
                          .optionalFieldOf("trickle_ticks", PLAIN_TRICKLE_TICKS)
                          .forGetter(HunterBuild::trickleTicks),
                      Codec.intRange(0, MOST_EXPERIENCE)
                          .optionalFieldOf("stake_experience", 0)
                          .forGetter(HunterBuild::stakeExperience),
                      Codec.unboundedMap(Identifier.CODEC, WEIGHT)
                          .optionalFieldOf("master_kits", Map.of())
                          .forGetter(HunterBuild::masterKits))
                  .apply(instance, HunterBuild::new));

  public HunterBuild {
    rankWeights = Map.copyOf(rankWeights);
    thinkTicks = Map.copyOf(thinkTicks);
    sharpness = Map.copyOf(sharpness);
    kitLeanings = Map.copyOf(kitLeanings);
    kitDispositions = Map.copyOf(kitDispositions);
    masterKits = Map.copyOf(masterKits);
  }

  public Mind mind(final Optional<Identifier> rank, final Optional<Identifier> kit) {
    return new Mind(
        rank.map(id -> thinkTicks.getOrDefault(id, PLAIN_THINK_TICKS)).orElse(PLAIN_THINK_TICKS),
        rank.map(id -> sharpness.getOrDefault(id, Sharpness.KEEN)).orElse(Sharpness.KEEN),
        kit.map(id -> kitDispositions.getOrDefault(id, Disposition.EVEN)).orElse(Disposition.EVEN));
  }

  public int weight(final Optional<Identifier> kit, final Identifier skill, final boolean path) {
    final int leaning =
        kit.map(id -> kitLeanings.getOrDefault(id, Map.of()).getOrDefault(skill, 0)).orElse(0);
    return (path ? pathWeight : coreWeight) + leaning;
  }

  public Optional<Identifier> masterKit(final Set<Identifier> carried, final double roll) {
    return Weighted.pick(
            new TreeMap<>(masterKits)
                .entrySet().stream().filter(entry -> carried.contains(entry.getKey())).toList(),
            Map.Entry::getValue,
            roll)
        .map(Map.Entry::getKey);
  }

  public boolean trickles(final long age) {
    return trickleExperience > 0 && age > 0 && age % trickleTicks == 0;
  }
}
