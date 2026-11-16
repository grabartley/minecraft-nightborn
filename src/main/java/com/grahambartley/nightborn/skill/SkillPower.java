package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.occult.FamiliarPower;
import com.grahambartley.nightborn.occult.FogPower;
import com.grahambartley.nightborn.occult.ShadowStepPower;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;

public record SkillPower(
    int blood,
    int cooldownTicks,
    List<StatusEffectInstance> effects,
    Optional<Identifier> sound,
    Optional<TrackPower> track,
    Optional<MesmerisePower> mesmerise,
    Optional<FamiliarPower> familiar,
    Optional<FogPower> fog,
    Optional<ShadowStepPower> shadowStep,
    Optional<MobPowerUse> mob) {

  public static final Codec<SkillPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(0, 20).fieldOf("blood").forGetter(SkillPower::blood),
                      Codec.intRange(0, 72000)
                          .fieldOf("cooldown_ticks")
                          .forGetter(SkillPower::cooldownTicks),
                      StatusEffectInstance.CODEC
                          .listOf()
                          .optionalFieldOf("effects", List.of())
                          .forGetter(SkillPower::effects),
                      Identifier.CODEC.optionalFieldOf("sound").forGetter(SkillPower::sound),
                      TrackPower.CODEC.optionalFieldOf("track").forGetter(SkillPower::track),
                      MesmerisePower.CODEC
                          .optionalFieldOf("mesmerise")
                          .forGetter(SkillPower::mesmerise),
                      FamiliarPower.CODEC
                          .optionalFieldOf("familiar")
                          .forGetter(SkillPower::familiar),
                      FogPower.CODEC.optionalFieldOf("fog").forGetter(SkillPower::fog),
                      ShadowStepPower.CODEC
                          .optionalFieldOf("shadow_step")
                          .forGetter(SkillPower::shadowStep),
                      MobPowerUse.CODEC.optionalFieldOf("mob").forGetter(SkillPower::mob))
                  .apply(instance, SkillPower::new));
}
