package com.grahambartley.nightborn.trap;

import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillRank;
import com.grahambartley.nightborn.skill.Skills;
import com.grahambartley.nightborn.skill.TreeProgress;
import java.util.List;
import java.util.Objects;
import net.minecraft.entity.LivingEntity;

public final class HeldTrapGifts {

  private HeldTrapGifts() {}

  public static List<TrapGift> of(final LivingEntity holder) {
    final TreeProgress owned = SkillAttachment.owned(holder);
    return Skills.skills(holder.getWorld().getRegistryManager())
        .map(
            registry ->
                owned.ownedInOrder().stream()
                    .map(id -> heldRanks(registry.get(id), owned.rank(id)))
                    .flatMap(List::stream)
                    .flatMap(rank -> rank.trapGifts().stream())
                    .toList())
        .orElse(List.of());
  }

  private static List<SkillRank> heldRanks(final Skill skill, final int rank) {
    return Objects.isNull(skill) ? List.of() : skill.held(rank);
  }
}
