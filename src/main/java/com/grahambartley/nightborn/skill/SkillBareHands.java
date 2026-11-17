package com.grahambartley.nightborn.skill;

import java.util.Collection;
import java.util.List;
import net.minecraft.entity.LivingEntity;

public final class SkillBareHands {

  private SkillBareHands() {}

  public static double damage(final LivingEntity entity) {
    final TreeProgress owned = SkillAttachment.owned(entity);
    return Skills.skills(entity.getWorld().getRegistryManager())
        .map(
            registry ->
                total(
                    owned.ownedInOrder().stream()
                        .flatMap(
                            id -> {
                              final Skill skill = registry.get(id);
                              return skill == null
                                  ? List.<SkillRank>of().stream()
                                  : skill.held(owned.rank(id)).stream();
                            })
                        .toList()))
        .orElse(0.0);
  }

  public static double total(final Collection<SkillRank> ranks) {
    return ranks.stream().mapToDouble(SkillRank::bareHandDamage).sum();
  }
}
