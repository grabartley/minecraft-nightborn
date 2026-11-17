package com.grahambartley.nightborn.mob.stage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public record VampireStage(
    int rank,
    int weight,
    StageBody body,
    StageSkills skills,
    StageCombat combat,
    StageLook look,
    Optional<RegistryKey<LootTable>> loot,
    Optional<StageAge> age) {

  public static final int HIGHEST_RANK = 15;

  public static final VampireStage PLAIN =
      new VampireStage(
          0,
          0,
          StageBody.SAME,
          StageSkills.NONE,
          StageCombat.PLAIN,
          StageLook.PLAIN,
          Optional.empty(),
          Optional.empty());

  public static final Codec<VampireStage> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(0, HIGHEST_RANK).fieldOf("rank").forGetter(VampireStage::rank),
                      Codec.intRange(0, StageOdds.HEAVIEST)
                          .fieldOf("weight")
                          .forGetter(VampireStage::weight),
                      StageBody.CODEC
                          .optionalFieldOf("body", StageBody.SAME)
                          .forGetter(VampireStage::body),
                      StageSkills.CODEC
                          .optionalFieldOf("skills", StageSkills.NONE)
                          .forGetter(VampireStage::skills),
                      StageCombat.CODEC
                          .optionalFieldOf("combat", StageCombat.PLAIN)
                          .forGetter(VampireStage::combat),
                      StageLook.CODEC
                          .optionalFieldOf("look", StageLook.PLAIN)
                          .forGetter(VampireStage::look),
                      RegistryKey.createCodec(RegistryKeys.LOOT_TABLE)
                          .optionalFieldOf("loot")
                          .forGetter(VampireStage::loot),
                      StageAge.CODEC.optionalFieldOf("age").forGetter(VampireStage::age))
                  .apply(instance, VampireStage::new));
}
