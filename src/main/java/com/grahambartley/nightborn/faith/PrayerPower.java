package com.grahambartley.nightborn.faith;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public record PrayerPower(
    float radius,
    float health,
    float healthPerSecond,
    int durationTicks,
    boolean shelters,
    float harmPerSecond,
    Optional<RegistryKey<DamageType>> harmType,
    Optional<Float> spotRange,
    float height,
    Optional<Identifier> look) {

  public static final Codec<PrayerPower> CODEC =
      RecordCodecBuilder.<PrayerPower>create(
              instance ->
                  instance
                      .group(
                          Codec.floatRange(1.0F, 16.0F)
                              .fieldOf("radius")
                              .forGetter(PrayerPower::radius),
                          Codec.floatRange(0.0F, 40.0F)
                              .optionalFieldOf("health", 0.0F)
                              .forGetter(PrayerPower::health),
                          Codec.floatRange(0.0F, 20.0F)
                              .optionalFieldOf("health_per_second", 0.0F)
                              .forGetter(PrayerPower::healthPerSecond),
                          Codec.intRange(0, 6000)
                              .optionalFieldOf("duration_ticks", 0)
                              .forGetter(PrayerPower::durationTicks),
                          Codec.BOOL
                              .optionalFieldOf("shelters", false)
                              .forGetter(PrayerPower::shelters),
                          Codec.floatRange(0.0F, 20.0F)
                              .optionalFieldOf("harm_per_second", 0.0F)
                              .forGetter(PrayerPower::harmPerSecond),
                          RegistryKey.createCodec(RegistryKeys.DAMAGE_TYPE)
                              .optionalFieldOf("harm_type")
                              .forGetter(PrayerPower::harmType),
                          Codec.floatRange(1.0F, 32.0F)
                              .optionalFieldOf("spot_range")
                              .forGetter(PrayerPower::spotRange),
                          Codec.floatRange(1.0F, 32.0F)
                              .optionalFieldOf("height", 8.0F)
                              .forGetter(PrayerPower::height),
                          Identifier.CODEC.optionalFieldOf("look").forGetter(PrayerPower::look))
                      .apply(instance, PrayerPower::new))
          .validate(PrayerPower::whole);

  public boolean lingers() {
    return durationTicks > 0;
  }

  private static DataResult<PrayerPower> whole(final PrayerPower power) {
    if (power.health() <= 0.0F && !power.lingers()) {
      return DataResult.error(() -> "A prayer heals at once or lasts a while");
    }
    if (power.lingers()
        && power.healthPerSecond() <= 0.0F
        && power.harmPerSecond() <= 0.0F
        && !power.shelters()) {
      return DataResult.error(() -> "A lasting prayer heals, harms or shelters");
    }
    if (power.harmPerSecond() > 0.0F && power.harmType().isEmpty()) {
      return DataResult.error(() -> "A prayer that harms names its damage type");
    }
    return DataResult.success(power);
  }
}
