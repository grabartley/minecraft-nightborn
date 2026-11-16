package com.grahambartley.nightborn.skill;

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
    Optional<BatFormPower> batForm,
    Optional<LeapPower> leap,
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
                      BatFormPower.CODEC.optionalFieldOf("bat_form").forGetter(SkillPower::batForm),
                      LeapPower.CODEC.optionalFieldOf("leap").forGetter(SkillPower::leap),
                      MobPowerUse.CODEC.optionalFieldOf("mob").forGetter(SkillPower::mob))
                  .apply(instance, SkillPower::new));
}
