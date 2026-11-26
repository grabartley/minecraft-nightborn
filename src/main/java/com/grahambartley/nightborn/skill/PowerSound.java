package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public record PowerSound(Identifier id, float volume, float pitch, float pitchSpread) {

  private static final float QUIETEST = 0.05F;
  private static final float FULL_VOLUME = 1.0F;
  private static final float LOWEST_PITCH = 0.5F;
  private static final float PLAIN_PITCH = 1.0F;
  private static final float HIGHEST_PITCH = 2.0F;
  private static final float PITCH_SPREAD = 0.1F;
  private static final float WIDEST_SPREAD = 0.5F;

  private static final Codec<PowerSound> FULL =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("id").forGetter(PowerSound::id),
                      Codec.floatRange(QUIETEST, FULL_VOLUME)
                          .optionalFieldOf("volume", FULL_VOLUME)
                          .forGetter(PowerSound::volume),
                      Codec.floatRange(LOWEST_PITCH, HIGHEST_PITCH)
                          .optionalFieldOf("pitch", PLAIN_PITCH)
                          .forGetter(PowerSound::pitch),
                      Codec.floatRange(0.0F, WIDEST_SPREAD)
                          .optionalFieldOf("pitch_spread", PITCH_SPREAD)
                          .forGetter(PowerSound::pitchSpread))
                  .apply(instance, PowerSound::new));

  public static final Codec<PowerSound> CODEC =
      Codec.withAlternative(FULL, Identifier.CODEC.xmap(PowerSound::of, PowerSound::id));

  public static PowerSound of(final Identifier id) {
    return new PowerSound(id, FULL_VOLUME, PLAIN_PITCH, PITCH_SPREAD);
  }

  public PowerSound atVolume(final float softer) {
    return new PowerSound(id, softer, pitch, pitchSpread);
  }

  public float pitchFor(final float roll) {
    final float shifted =
        pitch + (Math.max(0.0F, Math.min(1.0F, roll)) * 2.0F - 1.0F) * pitchSpread;
    return Math.max(LOWEST_PITCH, Math.min(HIGHEST_PITCH, shifted));
  }
}
