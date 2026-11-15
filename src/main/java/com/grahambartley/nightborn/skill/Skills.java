package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.Nightborn;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
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
    ServerLifecycleEvents.SERVER_STARTED.register(server -> check(server.getRegistryManager()));
  }

  static void check(final DynamicRegistryManager registries) {
    final Map<Identifier, Skill> skills = new HashMap<>();
    final Map<Identifier, SkillBranch> branches = new HashMap<>();
    skills(registries).ifPresent(r -> r.getIds().forEach(id -> skills.put(id, r.get(id))));
    branches(registries).ifPresent(r -> r.getIds().forEach(id -> branches.put(id, r.get(id))));
    SkillDataCheck.problems(skills, branches)
        .forEach(problem -> Nightborn.LOGGER.error("Bad skill data: {}", problem));
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

  public record Lookup(
      Identifier id,
      @Nullable Skill skill,
      @Nullable SkillBranch branch,
      SkillPurchase.Verdict verdict) {}

  public static Lookup lookup(
      final DynamicRegistryManager registries,
      final TreeProgress progress,
      final SkillTree playerTree,
      final Identifier id) {
    final Skill skill = skill(registries, id);
    final SkillBranch branch =
        skill == null
            ? null
            : branches(registries).map(registry -> registry.get(skill.branch())).orElse(null);
    return new Lookup(
        id, skill, branch, SkillPurchase.check(progress, playerTree, id, skill, branch));
  }
}
