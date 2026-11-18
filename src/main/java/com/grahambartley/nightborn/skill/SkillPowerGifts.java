package com.grahambartley.nightborn.skill;

import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class SkillPowerGifts {

  private SkillPowerGifts() {}

  public static List<PowerGift> forPower(final LivingEntity caster, @Nullable final Identifier id) {
    if (id == null) {
      return List.of();
    }
    final TreeProgress owned = SkillAttachment.owned(caster);
    return Skills.skills(caster.getWorld().getRegistryManager())
        .map(
            registry -> {
              final Skill power = registry.get(id);
              if (power == null) {
                return List.<PowerGift>of();
              }
              return covering(
                  power.branch(),
                  owned.ownedInOrder().stream()
                      .map(registry::get)
                      .filter(skill -> skill != null)
                      .flatMap(skill -> skill.held(owned.rank(registry.getId(skill))).stream())
                      .flatMap(rank -> rank.powerGifts().stream())
                      .toList());
            })
        .orElse(List.of());
  }

  public static List<PowerGift> covering(final Identifier branch, final List<PowerGift> gifts) {
    return gifts.stream().filter(gift -> gift.covers(branch)).toList();
  }
}
