package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public record ZoneHit(
    Optional<RegistryKey<DamageType>> damageType,
    List<StatusEffectInstance> effects,
    Optional<ZoneWeaken> weaken,
    float igniteSeconds,
    int sunTicks) {

  public static final ZoneHit PLAIN =
      new ZoneHit(Optional.empty(), List.of(), Optional.empty(), 0.0F, 0);

  public static final Codec<ZoneHit> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      RegistryKey.createCodec(RegistryKeys.DAMAGE_TYPE)
                          .optionalFieldOf("damage_type")
                          .forGetter(ZoneHit::damageType),
                      StatusEffectInstance.CODEC
                          .listOf()
                          .optionalFieldOf("effects", List.of())
                          .forGetter(ZoneHit::effects),
                      ZoneWeaken.CODEC.optionalFieldOf("weaken").forGetter(ZoneHit::weaken),
                      Codec.floatRange(0.0F, 30.0F)
                          .optionalFieldOf("ignite_seconds", 0.0F)
                          .forGetter(ZoneHit::igniteSeconds),
                      Codec.intRange(0, 1200)
                          .optionalFieldOf("sun_ticks", 0)
                          .forGetter(ZoneHit::sunTicks))
                  .apply(instance, ZoneHit::new));

  public ZoneHit {
    effects = List.copyOf(effects);
  }

  public static boolean wounds(final float damage) {
    return damage > 0.0F;
  }

  public static boolean lays(final boolean wounds, final boolean landed) {
    return landed || !wounds;
  }
}
