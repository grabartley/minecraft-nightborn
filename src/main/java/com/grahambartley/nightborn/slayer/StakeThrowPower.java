package com.grahambartley.nightborn.slayer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public record StakeThrowPower(
    float range, float speed, float damage, int staggerTicks, RegistryKey<DamageType> damageType) {

  public static final Codec<StakeThrowPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, 64.0F)
                          .fieldOf("range")
                          .forGetter(StakeThrowPower::range),
                      Codec.floatRange(0.5F, 4.0F)
                          .fieldOf("speed")
                          .forGetter(StakeThrowPower::speed),
                      Codec.floatRange(0.0F, 40.0F)
                          .fieldOf("damage")
                          .forGetter(StakeThrowPower::damage),
                      Codec.intRange(0, 200)
                          .fieldOf("stagger_ticks")
                          .forGetter(StakeThrowPower::staggerTicks),
                      RegistryKey.createCodec(RegistryKeys.DAMAGE_TYPE)
                          .fieldOf("damage_type")
                          .forGetter(StakeThrowPower::damageType))
                  .apply(instance, StakeThrowPower::new));

  public int flightTicks() {
    return (int) Math.ceil(range / speed);
  }
}
