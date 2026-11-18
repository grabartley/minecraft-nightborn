package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

public record WardAttack(
    RegistryEntry<EntityType<?>> entity,
    int count,
    float spread,
    int ticks,
    boolean shields,
    int limit) {

  public static final Codec<WardAttack> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Registries.ENTITY_TYPE
                          .getEntryCodec()
                          .fieldOf("entity")
                          .forGetter(WardAttack::entity),
                      Codec.intRange(1, 8).fieldOf("count").forGetter(WardAttack::count),
                      Codec.floatRange(0.5F, 8.0F)
                          .optionalFieldOf("spread", 3.0F)
                          .forGetter(WardAttack::spread),
                      Codec.intRange(20, 6000).fieldOf("ticks").forGetter(WardAttack::ticks),
                      Codec.BOOL.optionalFieldOf("shields", true).forGetter(WardAttack::shields),
                      Codec.intRange(0, 16)
                          .optionalFieldOf("limit", 0)
                          .forGetter(WardAttack::limit))
                  .apply(instance, WardAttack::new));

  public boolean gathers() {
    return limit > 0;
  }

  public int called(final int alive) {
    return gathers() ? Math.max(0, Math.min(count, limit - alive)) : count;
  }

  public double offsetX(final int index) {
    return Math.cos(2.0 * Math.PI * index / count) * spread;
  }

  public double offsetZ(final int index) {
    return Math.sin(2.0 * Math.PI * index / count) * spread;
  }

  public static boolean spent(final int alive, final long age, final int ticks) {
    return alive > 0 && age >= ticks;
  }

  public static boolean holds(final int alive, final long age, final int ticks) {
    return alive > 0 && age < ticks;
  }
}
