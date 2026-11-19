package com.grahambartley.nightborn.trap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.Identifier;

public record TrapGift(
    Optional<Identifier> power,
    double armCut,
    Optional<Float> seenWithin,
    int extraLimit,
    float extraDamage,
    boolean senses,
    double pinnedDamage) {

  public static final Codec<TrapGift> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.optionalFieldOf("power").forGetter(TrapGift::power),
                      Codec.doubleRange(0.0, 1.0)
                          .optionalFieldOf("arm_cut", 0.0)
                          .forGetter(TrapGift::armCut),
                      Codec.floatRange(0.0F, 64.0F)
                          .optionalFieldOf("seen_within")
                          .forGetter(TrapGift::seenWithin),
                      Codec.intRange(0, 8)
                          .optionalFieldOf("extra_limit", 0)
                          .forGetter(TrapGift::extraLimit),
                      Codec.floatRange(0.0F, 20.0F)
                          .optionalFieldOf("extra_damage", 0.0F)
                          .forGetter(TrapGift::extraDamage),
                      Codec.BOOL.optionalFieldOf("senses", false).forGetter(TrapGift::senses),
                      Codec.doubleRange(0.0, 4.0)
                          .optionalFieldOf("pinned_damage", 0.0)
                          .forGetter(TrapGift::pinnedDamage))
                  .apply(instance, TrapGift::new));

  public boolean covers(final Identifier powerId) {
    return power.map(powerId::equals).orElse(true);
  }
}
