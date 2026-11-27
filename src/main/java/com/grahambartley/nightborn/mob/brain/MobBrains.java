package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.skill.Skills;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

public final class MobBrains {

  private MobBrains() {}

  public static void register() {
    MobIntents.register();
    ServerLifecycleEvents.SERVER_STARTED.register(
        server -> {
          final DynamicRegistryManager registries = server.getRegistryManager();
          final MobUseCheck.Report report =
              MobUseCheck.check(Skills.allSkills(registries), budgets(registries));
          report.unused().forEach(line -> Nightborn.LOGGER.warn("Mob powers: {}", line));
          report.problems().forEach(line -> Nightborn.LOGGER.error("Bad mob power data: {}", line));
        });
  }

  private static Set<Identifier> budgets(final DynamicRegistryManager registries) {
    final Set<Identifier> ids = new HashSet<>();
    registries.getOptional(MobIntents.BUDGET).ifPresent(registry -> ids.addAll(registry.getIds()));
    return ids;
  }
}
