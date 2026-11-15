package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.faction.FactionAttachment;
import com.grahambartley.nightborn.vampire.Modifiers;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class SkillAttributes {

  private SkillAttributes() {}

  public static void apply(final PlayerEntity player) {
    final Registry<Skill> skills =
        Skills.skills(player.getWorld().getRegistryManager()).orElse(null);
    if (skills == null) {
      return;
    }
    final TreeProgress owned =
        SkillAttachment.get(player, SkillTree.of(FactionAttachment.get(player)));
    for (final Identifier id : skills.getIds()) {
      final List<SkillAttribute> attributes = skills.get(id).attributes();
      final boolean on = owned.owns(id);
      for (int i = 0; i < attributes.size(); i++) {
        final SkillAttribute attribute = attributes.get(i);
        final Identifier modifier = modifierId(id, i);
        if (on) {
          Modifiers.set(
              player, attribute.attribute(), modifier, attribute.amount(), attribute.operation());
        } else {
          Modifiers.remove(player, attribute.attribute(), modifier);
        }
      }
    }
    if (player.getHealth() > player.getMaxHealth()) {
      player.setHealth(player.getMaxHealth());
    }
  }

  static Identifier modifierId(final Identifier skill, final int index) {
    return Identifier.of(skill.getNamespace(), "skill/" + skill.getPath() + "/" + index);
  }
}
