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
    ServerLifecycleEvents.SERVER_STARTED.register(
        server ->
            SkillDataCheck.problems(
                    allSkills(server.getRegistryManager()),
                    allBranches(server.getRegistryManager()))
                .forEach(problem -> Nightborn.LOGGER.error("Bad skill data: {}", problem)));
  }

  public static Optional<Registry<Skill>> skills(final DynamicRegistryManager registries) {
    return registries.getOptional(SKILL);
  }

  public static Optional<Registry<SkillBranch>> branches(final DynamicRegistryManager registries) {
    return registries.getOptional(BRANCH);
  }

  public static Map<Identifier, Skill> allSkills(final DynamicRegistryManager registries) {
    return copy(skills(registries));
  }

  public static Map<Identifier, SkillBranch> allBranches(final DynamicRegistryManager registries) {
    return copy(branches(registries));
  }

  @Nullable
  public static Skill skill(final DynamicRegistryManager registries, final Identifier id) {
    return skills(registries).map(registry -> registry.get(id)).orElse(null);
  }

  @Nullable
  public static SkillBranch branch(final DynamicRegistryManager registries, final Identifier id) {
    return branches(registries).map(registry -> registry.get(id)).orElse(null);
  }

  private static <T> Map<Identifier, T> copy(final Optional<Registry<T>> registry) {
    final Map<Identifier, T> entries = new HashMap<>();
    registry.ifPresent(r -> r.getIds().forEach(id -> entries.put(id, r.get(id))));
    return entries;
  }
}
