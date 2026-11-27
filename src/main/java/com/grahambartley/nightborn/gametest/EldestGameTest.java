package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.gear.Staking;
import com.grahambartley.nightborn.mind.Mesmerise;
import com.grahambartley.nightborn.mob.EldestEntity;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.goal.HouseholdLifeGoal;
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
import com.grahambartley.nightborn.structure.HeadSeat;
import com.grahambartley.nightborn.structure.VampireStructure;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.SpawnReason;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;

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
    final long bloodlines =
        branches.values().stream().filter(b -> b.line() && b.tree() == SkillTree.VAMPIRE).count();
    context.assertTrue(
        learned.lines().size() == bloodlines,
        "it holds " + learned.lines().size() + " of " + bloodlines + " bloodlines");
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
        !eldest.presence().bar().getPlayers().contains(player),
        "seeing it from outside its arena shows no bar");
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
  public void theEldestKeepsAfterAHumanWhoNeverStruckIt(final TestContext context) {
    final EldestEntity eldest = eldest(context);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 2));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          eldest.setTarget(human);
          eldest.tickHome();
          context.assertTrue(
              eldest.getTarget() == human, "the Eldest keeps a human it attacks on sight");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 200)
  public void stakingTheEldestPaysEveryHumanWhoFought(final TestContext context) {
    final EldestEntity eldest = eldest(context);
    final ServerPlayerEntity staker = TestPlayers.hunter(context, new BlockPos(2, 2, 2));
    final ServerPlayerEntity helper = TestPlayers.hunter(context, new BlockPos(6, 2, 2));
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
          context.assertTrue(!eldest.isAlive(), "the stake ends the Eldest");
          TestPlayers.assertPaid(
              context,
              helper,
              SkillTree.HUNTER,
              TreeProgress.EMPTY,
              config.ritual().eldestExperience(),
              "the helper is paid for the Eldest");
          TestPlayers.assertPaid(
              context,
              staker,
              SkillTree.HUNTER,
              TreeProgress.EMPTY,
              config.ritual().eldestExperience()
                  + SkillEarning.huntExperience(config.skills(), eldest.stageRank()),
              "the staker is paid for the Eldest and the staking");
          context.assertTrue(points(watcher) == 0, "a bystander earns nothing");
          TestPlayers.finish(context, staker, helper, watcher);
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 200)
  public void theSunEndingTheEldestPaysTheHumanWhoFought(final TestContext context) {
    final EldestEntity eldest = eldest(context);
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(2, 2, 2));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          eldest.damage(context.getWorld().getDamageSources().playerAttack(hunter), 1.0F);
          eldest.timeUntilRegen = 0;
          eldest.damage(NightbornDamage.sun(context.getWorld()), Float.MAX_VALUE);
        });
    context.runAtTick(
        TestPlayers.READY + 5,
        () -> {
          context.assertTrue(!eldest.isAlive(), "the sun ends the Eldest");
          TestPlayers.assertPaid(
              context,
              hunter,
              SkillTree.HUNTER,
              TreeProgress.EMPTY,
              ServerConfigService.current().ritual().eldestExperience(),
              "the hunter is paid for the Eldest");
          TestPlayers.finish(context, hunter);
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

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void anEldestSettledInACastleKeepsToItsRoom(final TestContext context) {
    final EldestEntity eldest = eldest(context);
    final BlockPos home = context.getAbsolutePos(CENTRE).down(20);
    eldest.settleAt(home);
    context.assertEquals(
        eldest.homeLife().home(), Optional.of(home), "it shares the household's home");
    context.assertEquals(
        eldest.homeLife().room(),
        Optional.of(eldest.getBlockPos()),
        "its room is where it was settled");
    context.assertTrue(
        eldest.goalSelector.getGoals().stream()
            .noneMatch(goal -> goal.getGoal() instanceof HouseholdLifeGoal),
        "it takes no part in the household's routine");
    context.assertTrue(!eldest.homeLife().out(eldest, true), "it never goes out to feed");
    final NbtCompound saved = new NbtCompound();
    eldest.writeCustomDataToNbt(saved);
    final EldestEntity loaded = context.spawnEntity(NightbornMobs.ELDEST, CENTRE.north());
    loaded.setAiDisabled(true);
    loaded.readCustomDataFromNbt(saved);
    context.assertEquals(
        loaded.homeLife().room(), eldest.homeLife().room(), "its room survives a save and load");
    context.assertTrue(
        loaded.goalSelector.getGoals().stream()
            .noneMatch(goal -> goal.getGoal() instanceof HouseholdLifeGoal),
        "and it still keeps out of the routine");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aSummonedEldestHasNoRoom(final TestContext context) {
    final EldestEntity eldest = eldest(context);
    context.assertEquals(eldest.homeLife().room(), Optional.empty(), "it keeps to no room");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aCastlesEldestSizesItselfWhenAFightBegins(final TestContext context) {
    final EldestEntity eldest = eldest(context);
    eldest.settleAt(context.getAbsolutePos(CENTRE).down(20));
    final float alone = eldest.getMaxHealth();
    final ServerPlayerEntity first = context.createMockCreativeServerPlayerInWorld();
    final ServerPlayerEntity second = context.createMockCreativeServerPlayerInWorld();
    first.refreshPositionAndAngles(eldest.getX() + 1.0, eldest.getY(), eldest.getZ(), 0.0F, 0.0F);
    second.refreshPositionAndAngles(eldest.getX() - 1.0, eldest.getY(), eldest.getZ(), 0.0F, 0.0F);
    eldest.setTarget(first);
    context.assertTrue(
        eldest.getMaxHealth() > alone,
        "two players near make it tougher: " + alone + " to " + eldest.getMaxHealth());
    final float grown = eldest.getMaxHealth();
    eldest.setHealth(grown / 2.0F);
    eldest.setTarget(null);
    eldest.setTarget(second);
    context.assertTrue(
        eldest.getMaxHealth() == grown && eldest.getHealth() == grown / 2.0F,
        "a hurt Eldest that finds a new target is not healed");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void theShippedMountainCastleHoldsTwentyAndSeatsTheEldestOnTop(final TestContext context) {
    final var registries = context.getWorld().getRegistryManager();
    final Structure structure =
        registries
            .get(RegistryKeys.STRUCTURE)
            .get(Identifier.of(Nightborn.MOD_ID, "mountain_castle"));
    context.assertTrue(
        structure instanceof VampireStructure, "the mountain castle loads, got " + structure);
    final VampireStructure castle = (VampireStructure) structure;
    context.assertTrue(
        castle.household().residents().getMin() == 20
            && castle.household().residents().getMax() == 20,
        "twenty vampires live there");
    context.assertTrue(
        castle
            .household()
            .head()
            .filter(
                head ->
                    head.resident().value() == NightbornMobs.ELDEST
                        && head.seat() == HeadSeat.HIGHEST)
            .isPresent(),
        "the Eldest takes the highest room");
    context.assertTrue(castle.summit().isPresent(), "it looks for a summit");
    context.assertTrue(
        registries
                .get(RegistryKeys.STRUCTURE_SET)
                .get(Identifier.of(Nightborn.MOD_ID, "mountain_castle"))
            != null,
        "its structure set loads");
    context.assertTrue(
        context
                .getWorld()
                .getServer()
                .getReloadableRegistries()
                .getLootTable(
                    RegistryKey.of(
                        RegistryKeys.LOOT_TABLE,
                        Identifier.of(Nightborn.MOD_ID, "chests/mountain_castle")))
            != LootTable.EMPTY,
        "its chest loot loads");
    context.complete();
  }
}
