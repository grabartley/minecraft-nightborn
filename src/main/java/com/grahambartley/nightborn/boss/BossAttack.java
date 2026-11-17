package com.grahambartley.nightborn.boss;

import com.grahambartley.nightborn.skill.MesmerisePower;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;

public record BossAttack(
    float minRange,
    float maxRange,
    int weight,
    int cooldownTicks,
    int tellTicks,
    String tellClip,
    Identifier tellSound,
    String clip,
    Identifier sound,
    Optional<Identifier> power,
    Optional<ZoneAttack> zone,
    Optional<FlurryAttack> flurry,
    Optional<DrainAttack> drain,
    Optional<MesmerisePower> gaze,
    Optional<WardAttack> ward) {

  public static final Codec<BossAttack> CODEC =
      RecordCodecBuilder.<BossAttack>create(
              instance ->
                  instance
                      .group(
                          Codec.floatRange(0.0F, 64.0F)
                              .optionalFieldOf("min_range", 0.0F)
                              .forGetter(BossAttack::minRange),
                          Codec.floatRange(0.0F, 64.0F)
                              .optionalFieldOf("max_range", 64.0F)
                              .forGetter(BossAttack::maxRange),
                          Codec.intRange(1, 100)
                              .optionalFieldOf("weight", 1)
                              .forGetter(BossAttack::weight),
                          Codec.intRange(0, 6000)
                              .optionalFieldOf("cooldown_ticks", 0)
                              .forGetter(BossAttack::cooldownTicks),
                          Codec.intRange(1, 200)
                              .fieldOf("tell_ticks")
                              .forGetter(BossAttack::tellTicks),
                          Codec.STRING.fieldOf("tell_clip").forGetter(BossAttack::tellClip),
                          Identifier.CODEC.fieldOf("tell_sound").forGetter(BossAttack::tellSound),
                          Codec.STRING.fieldOf("clip").forGetter(BossAttack::clip),
                          Identifier.CODEC.fieldOf("sound").forGetter(BossAttack::sound),
                          Identifier.CODEC.optionalFieldOf("power").forGetter(BossAttack::power),
                          ZoneAttack.CODEC.optionalFieldOf("zone").forGetter(BossAttack::zone),
                          FlurryAttack.CODEC
                              .optionalFieldOf("flurry")
                              .forGetter(BossAttack::flurry),
                          DrainAttack.CODEC.optionalFieldOf("drain").forGetter(BossAttack::drain),
                          MesmerisePower.CODEC.optionalFieldOf("gaze").forGetter(BossAttack::gaze),
                          WardAttack.CODEC.optionalFieldOf("ward").forGetter(BossAttack::ward))
                      .apply(instance, BossAttack::new))
          .validate(BossAttack::oneKind);

  static DataResult<BossAttack> oneKind(final BossAttack attack) {
    final long kinds =
        Stream.of(attack.power, attack.zone, attack.flurry, attack.drain, attack.gaze, attack.ward)
            .filter(Optional::isPresent)
            .count();
    if (kinds != 1) {
      return DataResult.error(
          () ->
              "A boss attack needs exactly one of power, zone, flurry, drain, gaze or ward, got "
                  + kinds);
    }
    return attack.minRange <= attack.maxRange
        ? DataResult.success(attack)
        : DataResult.error(() -> "A boss attack needs min_range <= max_range");
  }

  public boolean reaches(final double distance) {
    return distance >= minRange && distance <= maxRange;
  }

  public int strikeTicks() {
    return flurry.map(FlurryAttack::ticks).or(() -> drain.map(DrainAttack::ticks)).orElse(0);
  }
}
