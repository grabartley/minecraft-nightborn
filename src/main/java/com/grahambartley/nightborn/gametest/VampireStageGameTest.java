package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.config.CombatSettings;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.stage.VampireStages;
import com.grahambartley.nightborn.mob.stage.VampireStaging;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.PowerCooldownAttachment;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import com.grahambartley.nightborn.structure.HouseholdArrival;
import com.grahambartley.nightborn.weakened.Weakened;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class VampireStageGameTest implements FabricGameTest {

  static final String RETREAT_BATCH = "vampire-stage-retreat";
  static final int TELL_LANDS = 40;

  private int retreatDistanceBefore;

  @BeforeBatch(batchId = RETREAT_BATCH)
  public void pinNightAndShortRetreats(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
    retreatDistanceBefore = ServerConfigService.current().combat().retreatDistance();
    ServerConfigService.update(
        world.getServer(), ServerConfigService.current().with(CombatSettings.RETREAT_DISTANCE, 4));
  }

  @AfterBatch(batchId = RETREAT_BATCH)
  public void restoreRetreats(final ServerWorld world) {
    WorldClock.release(world);
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current().with(CombatSettings.RETREAT_DISTANCE, retreatDistanceBefore));
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aHouseholdsStageOddsReachTheVampiresItSpawns(final TestContext context) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(4, 2, 4));
    vampire.setAiDisabled(true);
    vampire.initialize(
        context.getWorld(),
        context.getWorld().getLocalDifficulty(vampire.getBlockPos()),
        SpawnReason.STRUCTURE,
        new HouseholdArrival(Map.of(ANCIENT, 1)));
    context.assertEquals(vampire.stageId(), ANCIENT, "the household's only stage is picked");
    context.complete();
  }

  @GameTest(
      templateName = ARENA,
      batchId = RETREAT_BATCH,
      tickLimit = TestPlayers.READY + TELL_LANDS + 10)
  public void aBadlyHurtVampireFallsBackWithoutCasting(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(7, 2, 7));
    final VampireEntity vampire = fighter(context, new BlockPos(1, 2, 1));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.damage(hunter.getDamageSources().playerAttack(hunter), 1.0F);
          vampire.setHealth(vampire.getMaxHealth() * 0.1F);
          vampire.setTarget(hunter);
        });
    context.runAtTick(
        TestPlayers.READY + TELL_LANDS,
        () -> {
          context.assertFalse(
              vampire.hasStatusEffect(StatusEffects.SPEED), "a falling back vampire casts nothing");
          context.assertEquals(
              BloodAttachment.get(vampire), BloodAttachment.FULL, "and spends no blood");
          TestPlayers.finish(context, hunter);
        });
  }

  static final String ARENA = "nightborn:vampire_arena";
  static final Identifier FLEDGLING = Identifier.of(Nightborn.MOD_ID, "fledgling");
  static final Identifier ANCIENT = Identifier.of(Nightborn.MOD_ID, "ancient");
  static final Identifier BLOOD_RUSH = Identifier.of(Nightborn.MOD_ID, "core/blood_rush");
  static final Identifier THICK_BLOOD = Identifier.of(Nightborn.MOD_ID, "core/thick_blood");

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void anAncientIsTougherAndMoreSkilledThanAFledgling(final TestContext context) {
    final VampireEntity fledgling = staged(context, new BlockPos(2, 2, 4), FLEDGLING);
    final VampireEntity ancient = staged(context, new BlockPos(6, 2, 4), ANCIENT);
    context.assertEquals(fledgling.stageId(), FLEDGLING, "the fledgling keeps its stage");
    context.assertEquals(ancient.stageId(), ANCIENT, "the ancient keeps its stage");
    context.assertTrue(
        ancient.getMaxHealth() > fledgling.getMaxHealth(), "the ancient has more health");
    context.assertTrue(
        ancient.getHealth() == ancient.getMaxHealth(), "a staged vampire starts at full health");
    context.assertTrue(
        SkillAttachment.owned(ancient).bought().size()
            > SkillAttachment.owned(fledgling).bought().size(),
        "the ancient knows more skills");
    context.assertTrue(ancient.stageRank() > fledgling.stageRank(), "the ancient ranks higher");
    context.assertFalse(
        ancient.getLootTableId().equals(fledgling.getLootTableId()), "each stage drops its loot");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void everyShippedStageLoadsAsData(final TestContext context) {
    final var stages = VampireStages.stages(context.getWorld().getRegistryManager());
    context.assertTrue(
        stages.containsKey(FLEDGLING) && stages.containsKey(ANCIENT),
        "the shipped stages load into the world");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aVampireMobCastsBloodRushPayingBloodAndCoolingDown(final TestContext context) {
    final VampireEntity vampire = withBloodRush(context, new BlockPos(4, 2, 4));
    context.assertEquals(
        Powers.cast(vampire, BLOOD_RUSH), PowerUse.Verdict.READY, "the vampire casts");
    final int cost =
        Skills.skill(context.getWorld().getRegistryManager(), BLOOD_RUSH)
            .power()
            .orElseThrow()
            .blood();
    context.assertEquals(
        BloodAttachment.get(vampire), BloodAttachment.FULL - cost, "the cast costs blood");
    context.assertTrue(
        vampire.hasStatusEffect(StatusEffects.SPEED)
            && vampire.hasStatusEffect(StatusEffects.STRENGTH),
        "blood rush makes it faster and stronger");
    context.assertTrue(
        PowerCooldownAttachment.get(vampire).ticksLeft(BLOOD_RUSH, context.getWorld().getTime())
            > 0,
        "the power cools down");
    context.assertEquals(
        Powers.cast(vampire, BLOOD_RUSH), PowerUse.Verdict.COOLING, "it cannot cast again yet");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aWeakenedVampireMobCannotCast(final TestContext context) {
    final VampireEntity vampire = withBloodRush(context, new BlockPos(4, 2, 4));
    Weakened.add(vampire, "garlic", 30, 200);
    context.assertFalse(
        Powers.cast(vampire, BLOOD_RUSH) == PowerUse.Verdict.READY, "a weakened vampire is held");
    context.assertEquals(BloodAttachment.get(vampire), BloodAttachment.FULL, "no blood is spent");
    context.complete();
  }

  private static VampireEntity staged(
      final TestContext context, final BlockPos pos, final Identifier stage) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, pos);
    vampire.setAiDisabled(true);
    vampire.becomeStage(stage);
    return vampire;
  }

  private static VampireEntity fighter(final TestContext context, final BlockPos pos) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, pos);
    vampire.becomeStage(ANCIENT);
    learnBloodRush(context, vampire);
    return vampire;
  }

  private static void learnBloodRush(final TestContext context, final VampireEntity vampire) {
    final DynamicRegistryManager registries = context.getWorld().getRegistryManager();
    SkillAttachment.set(
        vampire,
        SkillTree.VAMPIRE,
        VampireStaging.learned(
            List.of(THICK_BLOOD, BLOOD_RUSH),
            Skills.allSkills(registries),
            Skills.allBranches(registries)));
    BloodAttachment.set(vampire, BloodAttachment.FULL);
  }

  private static VampireEntity withBloodRush(final TestContext context, final BlockPos pos) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, pos);
    vampire.setAiDisabled(true);
    learnBloodRush(context, vampire);
    return vampire;
  }
}
