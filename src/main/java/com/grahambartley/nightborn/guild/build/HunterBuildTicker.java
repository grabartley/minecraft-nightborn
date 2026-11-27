package com.grahambartley.nightborn.guild.build;

import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public final class HunterBuildTicker {

  private Map<Identifier, Integer> odds = Map.of();

  public void rollFrom(final Map<Identifier, Integer> wanted) {
    odds = Map.copyOf(wanted);
  }

  public boolean tick(
      final LivingEntity mob,
      final Optional<Identifier> fixedRank,
      final Optional<Identifier> kit) {
    final boolean begun = !HunterSkills.built(mob);
    if (begun) {
      HunterSkills.begin(mob, odds, fixedRank, kit);
    }
    return HunterSkills.trickle(mob, kit) || begun;
  }
}
