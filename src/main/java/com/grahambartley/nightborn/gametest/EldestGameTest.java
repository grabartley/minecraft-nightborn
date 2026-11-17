package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.gear.Staking;
import com.grahambartley.nightborn.mind.Mesmerise;
import com.grahambartley.nightborn.mob.EldestEntity;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.look.VampireRig;
import com.grahambartley.nightborn.mob.stage.SkillPicks;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillBranch;
import com.grahambartley.nightborn.skill.SkillEarning;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import com.grahambartley.nightborn.skill.TreeProgress;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class EldestGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String NIGHT_BATCH = "eldestNight";
  static final BlockPos CENTRE = new BlockPos(4, 2, 4);

  @BeforeBatch(batchId = NIGHT_BATCH)
  public void pinNight(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
  }

  @AfterBatch(batchId = NIGHT_BATCH)
  public void unpinNight(final ServerWorld world) {
    WorldClock.release(world);
  }

  static EldestEntity eldest(final TestContext context) {
    final EldestEntity eldest = context.spawnEntity(NightbornMobs.ELDEST, CENTRE);
    eldest.setAiDisabled(true);
    eldest.initialize(
        context.getWorld(),
        context.getWorld().getLocalDifficulty(eldest.getBlockPos()),
        SpawnReason.MOB_SUMMONED,
        null);
    return eldest;
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void anEldestHoldsEveryVampireSkill(final TestContext context) {
    final EldestEntity eldest = eldest(context);
    final var registries = context.getWorld().getRegistryManager();
    final Map<Identifier, SkillBranch> branches = Skills.allBranches(registries);
    final Map<Identifier, Skill> tree =
        SkillPicks.ofTree(Skills.allSkills(registries), branches, SkillTree.VAMPIRE);
    final TreeProgress learned = SkillAttachment.get(eldest, SkillTree.VAMPIRE);
    context.assertTrue(
        eldest.stageId().equals(EldestEntity.STAGE), "it is staged as " + eldest.stageId());
    context.assertTrue(!tree.isEmpty(), "the vampire tree loaded");
    for (final Identifier skill : tree.keySet()) {
      context.assertTrue(learned.owns(skill), "the Eldest lacks " + skill);
    }
    final long bloodlines = branches.values().stream().filter(SkillBranch::bloodline).count();
    context.assertTrue(
        learned.bloodlines().size() == bloodlines,
        "it holds " + learned.bloodlines().size() + " of " + bloodlines + " bloodlines");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void anEldestIsAVampireBossInItsOwnRig(final TestContext context) {
    final EldestEntity eldest = eldest(context);
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    context.assertTrue(VampireRig.ELDEST.contains(eldest.rig()), "it wears " + eldest.rig());
    context.assertTrue(Vampires.isVampire(eldest), "the Eldest is a vampire");
    context.assertTrue(
        eldest.getType().isIn(Mesmerise.UNMESMERISABLE), "the Eldest cannot be mesmerised");
    context.assertTrue(
        eldest.getMaxHealth() >= 250.0F, "it is tougher than a lord: " + eldest.getMaxHealth());
    context.assertTrue(eldest.cannotDespawn(), "the Eldest never despawns");
    eldest.onStartedTrackingBy(player);
    context.assertTrue(
        eldest.presence().bar().getPlayers().contains(player), "a player near it sees its bar");
    eldest.onStoppedTrackingBy(player);
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void morePlayersMakeATougherEldest(final TestContext context) {
    final EldestEntity eldest = eldest(context);
    final var settings = ServerConfigService.current().ritual();
    final float alone = eldest.getMaxHealth();
    eldest.scaleFor(3, settings);
    final float expected = alone * (1.0F + 2.0F * settings.eldestHealthPerPlayer() / 100.0F);
    context.assertTrue(
        Math.abs(eldest.getMaxHealth() - expected) < 0.01F,
        "three players raise " + alone + " to " + expected + ", not " + eldest.getMaxHealth());
    context.assertTrue(eldest.getHealth() == eldest.getMaxHealth(), "it starts at full health");
    eldest.scaleFor(1, settings);
    context.assertTrue(eldest.getMaxHealth() == alone, "one player leaves it as it was");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 200)
  public void stakingTheEldestPaysEveryHumanWhoFought(final TestContext context) {
    final EldestEntity eldest = eldest(context);
    final ServerPlayerEntity staker = TestPlayers.human(context, new BlockPos(2, 2, 2));
    final ServerPlayerEntity helper = TestPlayers.human(context, new BlockPos(6, 2, 2));
    final ServerPlayerEntity watcher = TestPlayers.human(context, new BlockPos(2, 2, 6));
    final var config = ServerConfigService.current();
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          eldest.damage(context.getWorld().getDamageSources().playerAttack(staker), 1.0F);
          eldest.timeUntilRegen = 0;
          eldest.damage(context.getWorld().getDamageSources().playerAttack(helper), 1.0F);
          context.assertTrue(
              eldest.fighters().equals(java.util.Set.of(staker.getUuid(), helper.getUuid())),
              "both attackers joined the fight: " + eldest.fighters());
          eldest.timeUntilRegen = 0;
          final var stake = NightbornDamage.stake(context.getWorld(), staker);
          context.assertTrue(Staking.staked(stake), "a stake counts as staking");
          eldest.damage(stake, Float.MAX_VALUE);
        });
    context.runAtTick(
        TestPlayers.READY + 5,
        () -> {
          final int eldestPoints = config.ritual().eldestPoints();
          final int stakePoints = SkillEarning.huntPoints(config.skills(), eldest.stageRank());
          context.assertTrue(!eldest.isAlive(), "the stake ends the Eldest");
          context.assertTrue(
              points(helper) == eldestPoints,
              "the helper earns " + eldestPoints + ", not " + points(helper));
          context.assertTrue(
              points(staker) == eldestPoints + stakePoints,
              "the staker earns " + (eldestPoints + stakePoints) + ", not " + points(staker));
          context.assertTrue(points(watcher) == 0, "a bystander earns nothing");
          TestPlayers.finish(context, staker, helper, watcher);
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 200)
  public void anEldestOnlyFightsAVampirePlayerWhoStruckIt(final TestContext context) {
    final EldestEntity eldest = eldest(context);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 2));
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(6, 2, 2));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertTrue(eldest.canTarget(human), "the Eldest fights a human");
          context.assertTrue(!eldest.canTarget(vampire), "it leaves a vampire player alone");
          eldest.damage(context.getWorld().getDamageSources().playerAttack(vampire), 1.0F);
          context.assertTrue(eldest.canTarget(vampire), "until that vampire strikes it");
          TestPlayers.finish(context, vampire, human);
        });
  }

  private static int points(final ServerPlayerEntity player) {
    return SkillAttachment.get(player, SkillTree.HUNTER).earned();
  }
}
