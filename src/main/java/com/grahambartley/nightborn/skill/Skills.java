package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.Nightborn;
import java.util.Optional;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class Skills {

  public static final RegistryKey<Registry<Skill>> SKILL =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "skill"));
  public static final RegistryKey<Registry<SkillBranch>> BRANCH =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "skill_branch"));

  private Skills() {}

  public static void register() {
    DynamicRegistries.registerSynced(SKILL, Skill.CODEC);
    DynamicRegistries.registerSynced(BRANCH, SkillBranch.CODEC);
  }

  public static Optional<Registry<Skill>> skills(final DynamicRegistryManager registries) {
    return registries.getOptional(SKILL);
  }

  public static Optional<Registry<SkillBranch>> branches(final DynamicRegistryManager registries) {
    return registries.getOptional(BRANCH);
  }

  @Nullable
  public static Skill skill(final DynamicRegistryManager registries, final Identifier id) {
    return skills(registries).map(registry -> registry.get(id)).orElse(null);
  }

  @Nullable
  public static SkillBranch branchOf(
      final DynamicRegistryManager registries, @Nullable final Skill skill) {
    return skill == null
        ? null
        : branches(registries).map(registry -> registry.get(skill.branch())).orElse(null);
  }

  public static SkillPurchase.Verdict check(
      final DynamicRegistryManager registries,
      final TreeProgress progress,
      final SkillTree playerTree,
      final Identifier id) {
    final Skill skill = skill(registries, id);
    return SkillPurchase.check(progress, playerTree, id, skill, branchOf(registries, skill));
  }
}
