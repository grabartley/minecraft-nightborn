package com.grahambartley.nightborn.guild.build;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.guild.GuildData;
import com.grahambartley.nightborn.rank.RankData;
import com.grahambartley.nightborn.skill.Skills;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class HunterBuilds {

  public static final RegistryKey<Registry<HunterBuild>> BUILD =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "hunter_build"));
  public static final Identifier HUNTER = Identifier.of(Nightborn.MOD_ID, "hunter");

  private HunterBuilds() {}

  public static void register() {
    DynamicRegistries.register(BUILD, HunterBuild.CODEC);
    HunterStakes.register();
    ServerLifecycleEvents.SERVER_STARTED.register(server -> check(server.getRegistryManager()));
    ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(
        (server, resources, success) -> check(server.getRegistryManager()));
  }

  private static void check(final DynamicRegistryManager registries) {
    HunterBuildCheck.problems(
            build(registries),
            RankData.ranks(registries).keySet(),
            Skills.allSkills(registries).keySet(),
            GuildData.kits(registries).keySet())
        .forEach(problem -> Nightborn.LOGGER.error("Bad hunter build data: {}", problem));
  }

  public static HunterBuild build(final DynamicRegistryManager registries) {
    return registries
        .getOptional(BUILD)
        .flatMap(registry -> registry.getOrEmpty(HUNTER))
        .orElse(HunterBuild.NONE);
  }
}
