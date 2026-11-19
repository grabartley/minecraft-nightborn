package com.grahambartley.nightborn.skill;

import java.util.List;
import java.util.function.Function;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public final class HeldRanks {

  private HeldRanks() {}

  public static List<SkillRank> of(final LivingEntity holder) {
    final TreeProgress owned = SkillAttachment.owned(holder);
    return Skills.skills(holder.getWorld().getRegistryManager())
        .map(registry -> held(owned, registry::get))
        .orElse(List.of());
  }

  public static List<SkillRank> held(
      final TreeProgress owned, final Function<Identifier, Skill> lookup) {
    return owned.ownedInOrder().stream()
        .flatMap(
            id -> {
              final Skill skill = lookup.apply(id);
              return skill == null
                  ? List.<SkillRank>of().stream()
                  : skill.held(owned.rank(id)).stream();
            })
        .toList();
  }
}
