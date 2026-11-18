package com.grahambartley.nightborn.rank;

import com.grahambartley.nightborn.config.SkillSettings;

public record RankGifts(double health, float vampireDamage) {

  public static final RankGifts NONE = new RankGifts(0.0, 0.0F);

  public static RankGifts of(final int rank, final SkillSettings settings) {
    final int above = Math.max(0, rank);
    return new RankGifts(
        (double) above * settings.rankHealth(), above * settings.rankDamageTenths() / 10.0F);
  }

  public float against(final boolean vampire) {
    return vampire ? vampireDamage : 0.0F;
  }
}
