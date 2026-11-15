package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.vampire.Modifiers;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class SkillAttributes {

  static final String PREFIX = "skill/";

  private SkillAttributes() {}

  public static void apply(final PlayerEntity player) {
    final Registry<Skill> skills =
        Skills.skills(player.getWorld().getRegistryManager()).orElse(null);
    final TreeProgress owned = SkillAttachment.get(player, SkillTree.of(player));
    final Map<Identifier, SkillAttribute> wanted = new HashMap<>();
    if (skills != null) {
      for (final Identifier id : owned.ownedInOrder()) {
        final Skill skill = skills.get(id);
        if (skill != null) {
          wanted.putAll(modifiers(id, skill.attributes()));
        }
      }
    }
    removeStray(player, wanted);
    wanted.forEach(
        (modifier, attribute) ->
            Modifiers.set(
                player,
                attribute.attribute(),
                modifier,
                attribute.amount(),
                attribute.operation()));
    Modifiers.clampHealth(player);
  }

  static Map<Identifier, SkillAttribute> modifiers(
      final Identifier skill, final List<SkillAttribute> attributes) {
    final Map<Identifier, SkillAttribute> modifiers = new HashMap<>();
    for (int i = 0; i < attributes.size(); i++) {
      modifiers.put(modifierId(skill, i), attributes.get(i));
    }
    return modifiers;
  }

  static Identifier modifierId(final Identifier skill, final int index) {
    return Identifier.of(
        Nightborn.MOD_ID, PREFIX + skill.getNamespace() + "/" + skill.getPath() + "/" + index);
  }

  static boolean stray(
      final Identifier modifier,
      final RegistryEntry<EntityAttribute> on,
      final Map<Identifier, SkillAttribute> wanted) {
    if (!modifier.getNamespace().equals(Nightborn.MOD_ID)
        || !modifier.getPath().startsWith(PREFIX)) {
      return false;
    }
    final SkillAttribute keep = wanted.get(modifier);
    return keep == null || !keep.attribute().equals(on);
  }

  private static void removeStray(
      final PlayerEntity player, final Map<Identifier, SkillAttribute> wanted) {
    Registries.ATTRIBUTE
        .streamEntries()
        .forEach(
            attribute -> {
              final EntityAttributeInstance instance = player.getAttributeInstance(attribute);
              if (instance == null) {
                return;
              }
              for (final EntityAttributeModifier modifier : List.copyOf(instance.getModifiers())) {
                if (stray(modifier.id(), attribute, wanted)) {
                  instance.removeModifier(modifier.id());
                }
              }
            });
  }
}
