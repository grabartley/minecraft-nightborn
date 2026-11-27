package com.grahambartley.nightborn.formview;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FormViewEcho(
    float nearRange,
    float nearBrightness,
    float rippleSpeed,
    float rippleSeconds,
    float rangePerVolume,
    float maxRange,
    float rippleWidth,
    float outline,
    float fill,
    float creatureBoost,
    int selfPulseTicks,
    float selfPulseVolume) {

  private static final Codec<Float> BLOCKS = Codec.floatRange(0.0F, 256.0F);
  private static final Codec<Float> AMOUNT = Codec.floatRange(0.0F, 4.0F);

  public static final Codec<FormViewEcho> CODEC =
      RecordCodecBuilder.<FormViewEcho>create(
              instance ->
                  instance
                      .group(
                          BLOCKS.fieldOf("near_range").forGetter(FormViewEcho::nearRange),
                          AMOUNT.fieldOf("near_brightness").forGetter(FormViewEcho::nearBrightness),
                          BLOCKS.fieldOf("ripple_speed").forGetter(FormViewEcho::rippleSpeed),
                          Codec.floatRange(0.05F, 30.0F)
                              .fieldOf("ripple_seconds")
                              .forGetter(FormViewEcho::rippleSeconds),
                          BLOCKS
                              .fieldOf("range_per_volume")
                              .forGetter(FormViewEcho::rangePerVolume),
                          BLOCKS.fieldOf("max_range").forGetter(FormViewEcho::maxRange),
                          Codec.floatRange(0.05F, 64.0F)
                              .fieldOf("ripple_width")
                              .forGetter(FormViewEcho::rippleWidth),
                          AMOUNT.fieldOf("outline").forGetter(FormViewEcho::outline),
                          AMOUNT.fieldOf("fill").forGetter(FormViewEcho::fill),
                          AMOUNT.fieldOf("creature_boost").forGetter(FormViewEcho::creatureBoost),
                          Codec.intRange(0, 1200)
                              .fieldOf("self_pulse_ticks")
                              .forGetter(FormViewEcho::selfPulseTicks),
                          AMOUNT
                              .fieldOf("self_pulse_volume")
                              .forGetter(FormViewEcho::selfPulseVolume))
                      .apply(instance, FormViewEcho::new))
          .validate(FormViewEcho::check);

  static DataResult<FormViewEcho> check(final FormViewEcho echo) {
    if (echo.rippleSpeed <= 0.0F) {
      return DataResult.error(() -> "ripple_speed must be above 0 or no ripple ever spreads");
    }
    if (echo.maxRange < echo.nearRange) {
      return DataResult.error(
          () ->
              "max_range "
                  + echo.maxRange
                  + " is shorter than near_range "
                  + echo.nearRange
                  + ": a ripple would never reach past the always-visible ground");
    }
    return DataResult.success(echo);
  }

  public float rippleRange(final float volume) {
    return Math.min(maxRange, Math.max(0.0F, volume) * rangePerVolume);
  }

  public boolean pulsesOn(final int tick) {
    return selfPulseTicks > 0 && selfPulseVolume > 0.0F && tick % selfPulseTicks == 0;
  }
}
