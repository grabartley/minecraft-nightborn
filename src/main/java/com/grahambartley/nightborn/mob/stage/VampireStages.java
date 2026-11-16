package com.grahambartley.nightborn.mob.stage;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class VampireStages {

  public static final RegistryKey<Registry<VampireStage>> STAGE =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "vampire_stage"));

  private VampireStages() {}

  public static void register() {
    DynamicRegistries.registerSynced(STAGE, VampireStage.CODEC);
    ServerLifecycleEvents.SERVER_STARTED.register(server -> check(server.getRegistryManager()));
    ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(
        (server, resources, success) -> check(server.getRegistryManager()));
  }

  private static void check(final DynamicRegistryManager registries) {
    StageDataCheck.problems(
            stages(registries),
            SkillPicks.ofTree(
                Skills.allSkills(registries), Skills.allBranches(registries), SkillTree.VAMPIRE))
        .forEach(problem -> Nightborn.LOGGER.error("Bad vampire stage data: {}", problem));
  }

  public static Map<Identifier, VampireStage> stages(final DynamicRegistryManager registries) {
    final Map<Identifier, VampireStage> stages = new HashMap<>();
    registries
        .getOptional(STAGE)
        .ifPresent(registry -> registry.getIds().forEach(id -> stages.put(id, registry.get(id))));
    return stages;
  }

  public static Optional<VampireStage> stage(
      final DynamicRegistryManager registries, final Identifier id) {
    return registries.getOptional(STAGE).flatMap(registry -> registry.getOrEmpty(id));
  }
}
