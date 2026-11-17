package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.MobSettings;
import com.grahambartley.nightborn.mind.Mesmerise;
import com.grahambartley.nightborn.mob.EldestEntity;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.goal.HouseholdLifeGoal;
import com.grahambartley.nightborn.mob.look.VampireRig;
import com.grahambartley.nightborn.mob.stage.SkillPicks;
import com.grahambartley.nightborn.mob.stage.VampireStages;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import com.grahambartley.nightborn.skill.TreeProgress;
import com.grahambartley.nightborn.structure.HeadSeat;
import com.grahambartley.nightborn.structure.VampireStructure;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.SpawnReason;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;

public class EldestGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final BlockPos CENTRE = new BlockPos(4, 2, 4);

  private static EldestEntity eldest(final TestContext context) {
    final EldestEntity eldest = context.spawnEntity(NightbornMobs.ELDEST, CENTRE);
    eldest.setAiDisabled(true);
    eldest.initialize(
        context.getWorld(),
        context.getWorld().getLocalDifficulty(eldest.getBlockPos()),
        SpawnReason.SPAWN_EGG,
        null);
    return eldest;
  }

  private static MobSettings crowdSettings(final int healthPerPlayer) {
    final MobSettings mobs = ServerConfigService.current().mobs();
    return new MobSettings(
        mobs.vampireBatSpawnChance(),
        mobs.batEscortChance(),
        mobs.vampireLevitateSeconds(),
        mobs.vampireLandSeconds(),
        mobs.vampireHissSeconds(),
        mobs.vampireHungerThreshold(),
        mobs.vampireBloodDrainSeconds(),
        mobs.vampireOutingChance(),
        mobs.vampireFeedingRange(),
        mobs.vampireChaseRange(),
        mobs.vampireHomeRadius(),
        mobs.vampireAlertRange(),
        healthPerPlayer,
        mobs.eldestCrowdRange());
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void theEldestHoldsEveryVampireSkill(final TestContext context) {
    final EldestEntity eldest = eldest(context);
    final var registries = context.getWorld().getRegistryManager();
    context.assertTrue(
        eldest.stageId().equals(EldestEntity.STAGE),
        "the Eldest takes its own stage, not " + eldest.stageId());
    final Set<Identifier> open =
        SkillPicks.forStage(
                SkillPicks.ofTree(
                    Skills.allSkills(registries),
                    Skills.allBranches(registries),
                    SkillTree.VAMPIRE),
                Skills.allBranches(registries),
                VampireStages.stage(registries, EldestEntity.STAGE).orElseThrow().skills())
            .keySet();
    final TreeProgress held = SkillAttachment.get(eldest, SkillTree.VAMPIRE);
    context.assertTrue(!open.isEmpty(), "the vampire tree has skills");
    for (final Identifier skill : open) {
      context.assertTrue(held.owns(skill), "the Eldest holds " + skill);
    }
    context.assertTrue(
        held.bloodlines().size() == 3,
        "the Eldest holds all three bloodlines, not " + held.bloodlines());
    context.assertTrue(Vampires.isVampire(eldest), "the Eldest is a vampire");
    context.assertTrue(VampireRig.COMMON.contains(eldest.rig()), "the Eldest is a man or a woman");
    context.assertTrue(
        eldest.getType().isIn(Mesmerise.UNMESMERISABLE), "the Eldest cannot be mesmerised");
    context.assertTrue(
        eldest.getMaxHealth() >= 300.0F,
        "the Eldest is tougher than a vampire lord, at " + eldest.getMaxHealth());
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void theEldestShowsItsBossBarOnlyToPlayersThatSeeIt(final TestContext context) {
    final EldestEntity eldest = eldest(context);
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    eldest.onStartedTrackingBy(player);
    context.assertTrue(
        eldest.presence().bar().getPlayers().contains(player), "a player near it sees its bar");
    eldest.onStoppedTrackingBy(player);
    context.assertTrue(
        eldest.presence().bar().getPlayers().isEmpty(), "a player who leaves no longer sees it");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void theEldestGrowsWithThePlayersNearIt(final TestContext context) {
    final EldestEntity eldest = eldest(context);
    final ServerPlayerEntity first = context.createMockCreativeServerPlayerInWorld();
    final ServerPlayerEntity second = context.createMockCreativeServerPlayerInWorld();
    first.refreshPositionAndAngles(eldest.getX() + 1.0, eldest.getY(), eldest.getZ(), 0.0F, 0.0F);
    second.refreshPositionAndAngles(eldest.getX() - 1.0, eldest.getY(), eldest.getZ(), 0.0F, 0.0F);
    eldest.gauge(crowdSettings(0));
    final float alone = eldest.getMaxHealth();
    eldest.gauge(crowdSettings(50));
    context.assertTrue(
        eldest.getMaxHealth() >= alone * 1.5F,
        "two players near it add at least half its health: "
            + alone
            + " to "
            + eldest.getMaxHealth());
    context.assertTrue(
        eldest.getHealth() == eldest.getMaxHealth(), "it gains the health it grew by");
    eldest.gauge(crowdSettings(0));
    context.assertTrue(
        eldest.getMaxHealth() == alone && eldest.getHealth() == alone,
        "with the setting at nothing it is back to its own health");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void theEldestKeepsToItsRoomAndOutOfTheHouseholdsRoutine(final TestContext context) {
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
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 40)
  public void everyHumanWhoFoughtTheEldestSharesItsReward(final TestContext context) {
    final EldestEntity eldest = eldest(context);
    final ServerPlayerEntity fighter = TestPlayers.human(context, new BlockPos(2, 2, 4));
    final ServerPlayerEntity staker = TestPlayers.human(context, new BlockPos(6, 2, 4));
    final ServerPlayerEntity watcher = TestPlayers.human(context, new BlockPos(4, 2, 6));
    final int reward = ServerConfigService.current().skills().eldestPoints();
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          eldest.damage(fighter.getDamageSources().playerAttack(fighter), 4.0F);
          context.assertTrue(eldest.isAlive(), "a blow does not end the Eldest");
          context.assertTrue(
              eldest.tookPart().contains(fighter.getUuid()), "the fighter is on its roll");
          eldest.damage(NightbornDamage.stake(context.getWorld(), staker), Float.MAX_VALUE);
          context.assertTrue(eldest.isDead(), "a stake ends the Eldest");
          context.assertEquals(
              SkillAttachment.get(fighter, SkillTree.HUNTER).earned(),
              reward,
              "the fighter is paid");
          context.assertEquals(
              SkillAttachment.get(staker, SkillTree.HUNTER).earned(), reward, "the staker is paid");
          context.assertEquals(
              SkillAttachment.get(watcher, SkillTree.HUNTER).earned(),
              0,
              "a human who took no part is not");
          TestPlayers.finish(context, fighter, staker, watcher);
        });
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
    for (final String table : new String[] {"chests/mountain_castle", "entities/vampire/eldest"}) {
      context.assertTrue(
          context
                  .getWorld()
                  .getServer()
                  .getReloadableRegistries()
                  .getLootTable(
                      RegistryKey.of(
                          RegistryKeys.LOOT_TABLE, Identifier.of(Nightborn.MOD_ID, table)))
              != LootTable.EMPTY,
          table + " loads");
    }
    context.complete();
  }
}
