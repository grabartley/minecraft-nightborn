package com.grahambartley.nightborn.guild.build;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.guild.GuildData;
import com.grahambartley.nightborn.guild.raiding.RaidingData;
import com.grahambartley.nightborn.guild.veteran.VeteranData;
import com.grahambartley.nightborn.mob.stage.SkillPicks;
import com.grahambartley.nightborn.rank.RankData;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
    final Set<Identifier> ranks = RankData.ranks(registries).keySet();
    final List<String> problems =
        new ArrayList<>(
            HunterBuildCheck.problems(
                build(registries),
                ranks,
                SkillPicks.ofTree(
                        Skills.allSkills(registries),
                        Skills.allBranches(registries),
                        SkillTree.HUNTER)
                    .keySet(),
                GuildData.kits(registries).keySet()));
    registries
        .getOptional(RaidingData.ARMS)
        .ifPresent(
            arms ->
                arms.getIds()
                    .forEach(
                        id ->
                            problems.addAll(
                                HunterBuildCheck.unknownRanks(
                                    id.toString(), arms.get(id).rankWeights().keySet(), ranks))));
    registries
        .getOptional(VeteranData.VETERAN)
        .ifPresent(
            veterans ->
                veterans
                    .getIds()
                    .forEach(
                        id ->
                            problems.addAll(
                                HunterBuildCheck.unknownRanks(
                                    id.toString(),
                                    veterans.get(id).rank().stream().collect(Collectors.toSet()),
                                    ranks))));
    problems.forEach(problem -> Nightborn.LOGGER.error("Bad hunter build data: {}", problem));
  }

  public static HunterBuild build(final DynamicRegistryManager registries) {
    return registries
        .getOptional(BUILD)
        .flatMap(registry -> registry.getOrEmpty(HUNTER))
        .orElse(HunterBuild.NONE);
  }
}
