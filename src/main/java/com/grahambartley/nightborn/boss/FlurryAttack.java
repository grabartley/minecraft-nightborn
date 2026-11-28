package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public record FlurryAttack(
    int hits,
    int everyTicks,
    float damage,
    float reach,
    float lunge,
    Optional<RegistryKey<DamageType>> damageType) {

  static final double ARC = 0.2;

  public static final Codec<FlurryAttack> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(1, 12).fieldOf("hits").forGetter(FlurryAttack::hits),
                      Codec.intRange(1, 100)
                          .fieldOf("every_ticks")
                          .forGetter(FlurryAttack::everyTicks),
                      Codec.floatRange(0.0F, 200.0F)
                          .fieldOf("damage")
                          .forGetter(FlurryAttack::damage),
                      Codec.floatRange(1.0F, 8.0F)
                          .optionalFieldOf("reach", 3.0F)
                          .forGetter(FlurryAttack::reach),
                      Codec.floatRange(0.0F, 3.0F)
                          .optionalFieldOf("lunge", 0.4F)
                          .forGetter(FlurryAttack::lunge),
                      RegistryKey.createCodec(RegistryKeys.DAMAGE_TYPE)
                          .optionalFieldOf("damage_type")
                          .forGetter(FlurryAttack::damageType))
                  .apply(instance, FlurryAttack::new));

  public int ticks() {
    return hits * everyTicks;
  }

  public boolean hitsAt(final int tick) {
    return tick % everyTicks == 0 && tick / everyTicks < hits;
  }

  public boolean reaches(
      final double facingX, final double facingZ, final double toX, final double toZ) {
    final double span = Math.sqrt(toX * toX + toZ * toZ);
    if (span > reach) {
      return false;
    }
    return span < 1.0E-6 || (facingX * toX + facingZ * toZ) / span >= ARC;
  }
}
