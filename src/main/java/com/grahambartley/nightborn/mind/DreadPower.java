package com.grahambartley.nightborn.mind;

import com.grahambartley.nightborn.skill.MobPowerUse;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public record DreadPower(
    float radius,
    int durationTicks,
    TagKey<EntityType<?>> flees,
    TagKey<EntityType<?>> cowers,
    TagKey<EntityType<?>> falters,
    float falterDamage,
    List<StatusEffectInstance> playerEffects) {

  public static final Codec<DreadPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, MobPowerUse.MAX_RANGE)
                          .fieldOf("radius")
                          .forGetter(DreadPower::radius),
                      Codec.intRange(1, 6000)
                          .fieldOf("duration_ticks")
                          .forGetter(DreadPower::durationTicks),
                      TagKey.unprefixedCodec(RegistryKeys.ENTITY_TYPE)
                          .fieldOf("flees")
                          .forGetter(DreadPower::flees),
                      TagKey.unprefixedCodec(RegistryKeys.ENTITY_TYPE)
                          .fieldOf("cowers")
                          .forGetter(DreadPower::cowers),
                      TagKey.unprefixedCodec(RegistryKeys.ENTITY_TYPE)
                          .fieldOf("falters")
                          .forGetter(DreadPower::falters),
                      Codec.floatRange(0.2F, 1.0F)
                          .fieldOf("falter_damage")
                          .forGetter(DreadPower::falterDamage),
                      StatusEffectInstance.CODEC
                          .listOf()
                          .optionalFieldOf("player_effects", List.of())
                          .forGetter(DreadPower::playerEffects))
                  .apply(instance, DreadPower::new));

  public DreadPower {
    playerEffects = List.copyOf(playerEffects);
  }
}
