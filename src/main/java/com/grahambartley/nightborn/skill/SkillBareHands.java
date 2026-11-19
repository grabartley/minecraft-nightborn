package com.grahambartley.nightborn.skill;

import java.util.Collection;
import net.minecraft.entity.LivingEntity;

public final class SkillBareHands {

  private SkillBareHands() {}

  public static double damage(final LivingEntity entity) {
    return total(HeldRanks.of(entity));
  }

  public static double total(final Collection<SkillRank> ranks) {
    return ranks.stream().mapToDouble(SkillRank::bareHandDamage).sum();
  }
}
