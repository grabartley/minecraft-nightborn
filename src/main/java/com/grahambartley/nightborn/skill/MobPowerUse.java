package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public record MobPowerUse(
    float minRange,
    float maxRange,
    int tellTicks,
    Identifier tellSound,
    String tellClip,
    boolean dodge) {

  public static final float MAX_RANGE = 64.0F;

  public static final Codec<MobPowerUse> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(0.0F, MAX_RANGE)
                          .optionalFieldOf("min_range", 0.0F)
                          .forGetter(MobPowerUse::minRange),
                      Codec.floatRange(0.0F, MAX_RANGE)
                          .fieldOf("max_range")
                          .forGetter(MobPowerUse::maxRange),
                      Codec.intRange(1, 100)
                          .fieldOf("tell_ticks")
                          .forGetter(MobPowerUse::tellTicks),
                      Identifier.CODEC.fieldOf("tell_sound").forGetter(MobPowerUse::tellSound),
                      Codec.STRING.fieldOf("tell_clip").forGetter(MobPowerUse::tellClip),
                      Codec.BOOL.optionalFieldOf("dodge", false).forGetter(MobPowerUse::dodge))
                  .apply(instance, MobPowerUse::new));

  public boolean inRange(final double distance) {
    return distance >= minRange && distance <= maxRange;
  }
}
