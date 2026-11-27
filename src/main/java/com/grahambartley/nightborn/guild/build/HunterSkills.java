package com.grahambartley.nightborn.guild.build;

import com.grahambartley.nightborn.age.StageRanks;
import com.grahambartley.nightborn.config.SkillSettings;
import com.grahambartley.nightborn.faction.OathAttachment;
import com.grahambartley.nightborn.mob.stage.StageGate;
import com.grahambartley.nightborn.rank.RankBody;
import com.grahambartley.nightborn.rank.RankLadder;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillAttributes;
import com.grahambartley.nightborn.skill.SkillBranch;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public final class HunterSkills {

  private HunterSkills() {}

  public static boolean built(final LivingEntity mob) {
    return OathAttachment.get(mob).sworn();
  }

  public static void begin(
      final LivingEntity mob,
      final Map<Identifier, Integer> odds,
      final Optional<Identifier> fixedRank,
      final Optional<Identifier> kit) {
    final DynamicRegistryManager registries = mob.getRegistryManager();
    final HunterBuild build = HunterBuilds.build(registries);
    final RankLadder ladder = RankLadder.of(registries);
    final Random random = mob.getRandom();
    final int rank =
        RankRoll.choose(ladder, fixedRank, odds, build.rankWeights(), random.nextDouble());
    final int points =
        RankRoll.points(settings().rankNeeds(), rank, build.topRankSpread(), random.nextDouble());
    final HunterPurchases.Shop shop = shop(mob, registries, build, ladder, kit);
    store(
        mob,
        HunterGrowth.begin(
            shop,
            rank,
            points,
            PathPick.pick(shop.branches(), SkillTree.HUNTER, random::nextDouble),
            random::nextDouble));
    mob.setHealth(mob.getMaxHealth());
  }

  public static void master(final LivingEntity mob) {
    final DynamicRegistryManager registries = mob.getRegistryManager();
    final RankLadder ladder = RankLadder.of(registries);
    final HunterPurchases.Shop shop =
        shop(mob, registries, HunterBuilds.build(registries), ladder, Optional.empty());
    store(
        mob,
        HunterGrowth.master(
            shop,
            ladder.size() - 1,
            PathPick.pick(shop.branches(), SkillTree.HUNTER, mob.getRandom()::nextDouble)));
    mob.setHealth(mob.getMaxHealth());
  }

  public static void earn(
      final LivingEntity mob, final float experience, final Optional<Identifier> kit) {
    if (!built(mob) || experience <= 0.0F) {
      return;
    }
    final DynamicRegistryManager registries = mob.getRegistryManager();
    final SkillSettings settings = settings();
    final HunterPurchases.Shop shop =
        shop(mob, registries, HunterBuilds.build(registries), RankLadder.of(registries), kit);
    store(
        mob,
        HunterGrowth.grow(
            standing(mob),
            experience,
            shop,
            new HunterGrowth.Curve(
                settings.firstPointExperience(), settings.pointCostRise(), settings.rankNeeds()),
            mob.getRandom()::nextDouble));
  }

  public static boolean trickle(final LivingEntity mob, final Optional<Identifier> kit) {
    final HunterBuild build = HunterBuilds.build(mob.getRegistryManager());
    if (!build.trickles(mob.age)) {
      return false;
    }
    earn(mob, build.trickleExperience(), kit);
    return true;
  }

  public static HunterGrowth.Standing standing(final LivingEntity mob) {
    return new HunterGrowth.Standing(
        OathAttachment.get(mob), SkillAttachment.get(mob, SkillTree.HUNTER));
  }

  public static Optional<Identifier> rank(final LivingEntity mob) {
    return RankLadder.of(mob.getRegistryManager()).markOf(OathAttachment.get(mob));
  }

  public static int thinkTicks(final LivingEntity mob, final Optional<Identifier> rank) {
    return HunterBuilds.build(mob.getRegistryManager()).thinkTicks(rank);
  }

  public static HunterPurchases.Shop shop(
      final LivingEntity mob,
      final DynamicRegistryManager registries,
      final HunterBuild build,
      final RankLadder ladder,
      final Optional<Identifier> kit) {
    final Map<Identifier, SkillBranch> branches = Skills.allBranches(registries);
    final Map<Identifier, Skill> skills = Skills.allSkills(registries);
    final int stage = StageRanks.of(mob);
    return new HunterPurchases.Shop(
        skills,
        branches,
        ladder,
        skill -> StageGate.open(StageGate.needed(registries, skill), stage),
        id -> build.weight(kit, id, HunterPurchases.onPath(skills.get(id), branches)));
  }

  private static void store(final LivingEntity mob, final HunterGrowth.Standing standing) {
    OathAttachment.set(mob, standing.oath());
    SkillAttachment.set(mob, SkillTree.HUNTER, standing.progress());
    RankBody.apply(mob, settings());
    SkillAttributes.apply(mob);
  }

  private static SkillSettings settings() {
    return ServerConfigService.current().skills();
  }
}
