package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PouncePower(float range, int pinTicks, int playerPinTicks) {

  public static final Codec<PouncePower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(2.0F, 32.0F).fieldOf("range").forGetter(PouncePower::range),
                      Codec.intRange(1, 600).fieldOf("pin_ticks").forGetter(PouncePower::pinTicks),
                      Codec.intRange(1, 600)
                          .fieldOf("player_pin_ticks")
                          .forGetter(PouncePower::playerPinTicks))
                  .apply(instance, PouncePower::new));

  public int pinFor(final boolean player) {
    return player ? playerPinTicks : pinTicks;
  }
}
