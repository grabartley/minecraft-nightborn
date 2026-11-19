package com.grahambartley.nightborn.trap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public record BaitPower(
    float range,
    int durationTicks,
    float lureRange,
    float scentRange,
    float size,
    Identifier look) {

  public static final Codec<BaitPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, 16.0F).fieldOf("range").forGetter(BaitPower::range),
                      Codec.intRange(20, 72000)
                          .fieldOf("duration_ticks")
                          .forGetter(BaitPower::durationTicks),
                      Codec.floatRange(1.0F, 64.0F)
                          .fieldOf("lure_range")
                          .forGetter(BaitPower::lureRange),
                      Codec.floatRange(1.0F, 64.0F)
                          .fieldOf("scent_range")
                          .forGetter(BaitPower::scentRange),
                      Codec.floatRange(0.1F, 4.0F)
                          .optionalFieldOf("size", 0.5F)
                          .forGetter(BaitPower::size),
                      Identifier.CODEC.fieldOf("look").forGetter(BaitPower::look))
                  .apply(instance, BaitPower::new));
}
