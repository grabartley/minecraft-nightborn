package com.grahambartley.nightborn.guild;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record HunterContract(
    Text description,
    ContractGoal goal,
    int count,
    int minimumStage,
    RegistryKey<LootTable> reward) {

  public static final int MOST_TARGETS = 64;

  public record Stake(int stageRank, boolean householdCleared) {}

  public static final Codec<HunterContract> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      TextCodecs.CODEC
                          .fieldOf("description")
                          .forGetter(HunterContract::description),
                      ContractGoal.CODEC.fieldOf("goal").forGetter(HunterContract::goal),
                      Codec.intRange(1, MOST_TARGETS)
                          .optionalFieldOf("count", 1)
                          .forGetter(HunterContract::count),
                      Codec.intRange(0, Integer.MAX_VALUE)
                          .optionalFieldOf("minimum_stage", 0)
                          .forGetter(HunterContract::minimumStage),
                      RegistryKey.createCodec(RegistryKeys.LOOT_TABLE)
                          .fieldOf("reward")
                          .forGetter(HunterContract::reward))
                  .apply(instance, HunterContract::new));

  public boolean countedBy(final Stake facts) {
    return facts.stageRank() >= minimumStage && goal.countedBy(facts);
  }

  public boolean finishedAt(final int done) {
    return done >= count;
  }
}
