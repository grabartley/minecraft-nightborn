package com.grahambartley.nightborn.blood;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public record ActiveGift(Identifier gift, float strength) {

  public static final Codec<ActiveGift> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("gift").forGetter(ActiveGift::gift),
                      Codec.floatRange(0.0F, BloodGift.FRESH)
                          .fieldOf("strength")
                          .forGetter(ActiveGift::strength))
                  .apply(instance, ActiveGift::new));

  public boolean refreshes(final Identifier other, final float otherStrength) {
    return gift.equals(other) && strength == otherStrength;
  }
}
