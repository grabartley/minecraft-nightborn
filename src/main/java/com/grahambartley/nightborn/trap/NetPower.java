package com.grahambartley.nightborn.trap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public record NetPower(
    float range, float speed, int groundTicks, RegistryKey<DamageType> damageType) {

  public static final Codec<NetPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, 64.0F).fieldOf("range").forGetter(NetPower::range),
                      Codec.floatRange(0.5F, 4.0F).fieldOf("speed").forGetter(NetPower::speed),
                      Codec.intRange(1, 1200)
                          .fieldOf("ground_ticks")
                          .forGetter(NetPower::groundTicks),
                      RegistryKey.createCodec(RegistryKeys.DAMAGE_TYPE)
                          .fieldOf("damage_type")
                          .forGetter(NetPower::damageType))
                  .apply(instance, NetPower::new));

  public int flightTicks() {
    return (int) Math.ceil(range / speed);
  }
}
