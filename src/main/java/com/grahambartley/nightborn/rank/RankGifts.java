package com.grahambartley.nightborn.rank;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RankGifts(double health, float vampireDamage) {

  public static final RankGifts NONE = new RankGifts(0.0, 0.0F);

  static final double MOST_HEALTH = 1000.0;
  static final float MOST_DAMAGE = 100.0F;

  public static final Codec<RankGifts> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.doubleRange(0.0, MOST_HEALTH)
                          .optionalFieldOf("health", 0.0)
                          .forGetter(RankGifts::health),
                      Codec.floatRange(0.0F, MOST_DAMAGE)
                          .optionalFieldOf("vampire_damage", 0.0F)
                          .forGetter(RankGifts::vampireDamage))
                  .apply(instance, RankGifts::new));

  public static RankGifts of(final RankLadder ladder, final boolean hunter, final int rank) {
    return hunter
        ? ladder
            .rung(Math.min(rank, ladder.size() - 1))
            .map(rung -> rung.rank().gifts())
            .orElse(NONE)
        : NONE;
  }

  public float against(final boolean vampire) {
    return vampire ? vampireDamage : 0.0F;
  }
}
