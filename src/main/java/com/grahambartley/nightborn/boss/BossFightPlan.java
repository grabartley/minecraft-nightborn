package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public record BossFightPlan(
    List<BossPhase> phases,
    int changeTicks,
    String changeClip,
    Identifier changeSound,
    float changeKnock,
    float changeRange,
    FightClips clips) {

  public static final Codec<BossFightPlan> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codecs.nonEmptyList(BossPhase.CODEC.listOf())
                          .fieldOf("phases")
                          .forGetter(BossFightPlan::phases),
                      Codec.intRange(0, 600)
                          .optionalFieldOf("change_ticks", 60)
                          .forGetter(BossFightPlan::changeTicks),
                      Codec.STRING.fieldOf("change_clip").forGetter(BossFightPlan::changeClip),
                      Identifier.CODEC
                          .fieldOf("change_sound")
                          .forGetter(BossFightPlan::changeSound),
                      Codec.floatRange(0.0F, 8.0F)
                          .optionalFieldOf("change_knock", 1.5F)
                          .forGetter(BossFightPlan::changeKnock),
                      Codec.floatRange(0.0F, 32.0F)
                          .optionalFieldOf("change_range", 6.0F)
                          .forGetter(BossFightPlan::changeRange),
                      FightClips.CODEC
                          .optionalFieldOf("clips", FightClips.NONE)
                          .forGetter(BossFightPlan::clips))
                  .apply(instance, BossFightPlan::new));

  public BossFightPlan {
    phases = List.copyOf(phases);
  }

  public List<Integer> shares() {
    return phases.stream().map(BossPhase::health).toList();
  }

  public BossPhase phase(final int index) {
    return phases.get(Math.max(0, Math.min(index, phases.size() - 1)));
  }
}
