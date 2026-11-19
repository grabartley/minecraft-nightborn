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
    return Skills.skills(caster.getWorld().getRegistryManager())
        .map(registry -> registry.get(id))
        .map(
            power ->
                covering(
                    power.branch(),
                    HeldRanks.of(caster).stream()
                        .flatMap(rank -> rank.powerGifts().stream())
                        .toList()))
        .orElse(List.of());
  }

  public static List<PowerGift> covering(final Identifier branch, final List<PowerGift> gifts) {
    return gifts.stream().filter(gift -> gift.covers(branch)).toList();
  }
}
