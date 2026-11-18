package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.skill.MobPowerUse;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.entity.effect.StatusEffectInstance;

public record HexPower(
    float range,
    int durationTicks,
    int playerDurationTicks,
    float healing,
    List<StatusEffectInstance> effects) {

  public static final Codec<HexPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, MobPowerUse.MAX_RANGE)
                          .fieldOf("range")
                          .forGetter(HexPower::range),
                      Codec.intRange(1, 6000)
                          .fieldOf("duration_ticks")
                          .forGetter(HexPower::durationTicks),
                      Codec.intRange(1, 6000)
                          .fieldOf("player_duration_ticks")
                          .forGetter(HexPower::playerDurationTicks),
                      Codec.floatRange(0.0F, 1.0F).fieldOf("healing").forGetter(HexPower::healing),
                      StatusEffectInstance.CODEC
                          .listOf()
                          .optionalFieldOf("effects", List.of())
                          .forGetter(HexPower::effects))
                  .apply(instance, HexPower::new));

  public HexPower {
    effects = List.copyOf(effects);
  }

  public int durationFor(final boolean player) {
    return player ? playerDurationTicks : durationTicks;
  }
}
