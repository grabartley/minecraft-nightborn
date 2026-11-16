package com.grahambartley.nightborn.skill;

import java.util.Collection;
import java.util.Objects;
import net.minecraft.entity.LivingEntity;

public final class SkillBareHands {

  private SkillBareHands() {}

  public static double damage(final LivingEntity entity) {
    return Skills.skills(entity.getWorld().getRegistryManager())
        .map(
            registry ->
                total(
                    SkillAttachment.owned(entity).ownedInOrder().stream()
                        .map(registry::get)
                        .filter(Objects::nonNull)
                        .toList()))
        .orElse(0.0);
  }

  public static double total(final Collection<Skill> skills) {
    return skills.stream().mapToDouble(Skill::bareHandDamage).sum();
  }
}
