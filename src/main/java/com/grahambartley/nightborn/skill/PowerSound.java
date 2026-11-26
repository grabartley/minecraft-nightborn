package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public record PowerSound(Identifier id, float volume, float pitch, float pitchSpread) {

  public static final float FULL_VOLUME = 1.0F;
  public static final float PLAIN_PITCH = 1.0F;
  public static final float PITCH_SPREAD = 0.1F;

  private static final Codec<PowerSound> FULL =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("id").forGetter(PowerSound::id),
                      Codec.floatRange(0.05F, FULL_VOLUME)
                          .optionalFieldOf("volume", FULL_VOLUME)
                          .forGetter(PowerSound::volume),
                      Codec.floatRange(0.5F, 2.0F)
                          .optionalFieldOf("pitch", PLAIN_PITCH)
                          .forGetter(PowerSound::pitch),
                      Codec.floatRange(0.0F, 0.5F)
                          .optionalFieldOf("pitch_spread", PITCH_SPREAD)
                          .forGetter(PowerSound::pitchSpread))
                  .apply(instance, PowerSound::new));

  public static final Codec<PowerSound> CODEC =
      Codec.withAlternative(FULL, Identifier.CODEC.xmap(PowerSound::of, PowerSound::id));

  public static PowerSound of(final Identifier id) {
    return new PowerSound(id, FULL_VOLUME, PLAIN_PITCH, PITCH_SPREAD);
  }

  public float pitchFor(final float roll) {
    return pitch + (Math.max(0.0F, Math.min(1.0F, roll)) * 2.0F - 1.0F) * pitchSpread;
  }
}
