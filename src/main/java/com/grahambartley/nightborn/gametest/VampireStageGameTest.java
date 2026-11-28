package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.brain.Intent;
import com.grahambartley.nightborn.mob.brain.Think;
import com.grahambartley.nightborn.mob.combat.MobFighting;
import com.grahambartley.nightborn.mob.goal.BatFlightGoal;
import com.grahambartley.nightborn.mob.stage.VampireStages;
import com.grahambartley.nightborn.mob.stage.VampireStaging;
import com.grahambartley.nightborn.skill.PowerCooldownAttachment;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import com.grahambartley.nightborn.skill.TreeProgress;
import com.grahambartley.nightborn.structure.HouseholdArrival;
import com.grahambartley.nightborn.weakened.Weakened;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class VampireStageGameTest implements FabricGameTest {

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aHouseholdsStageOddsReachTheVampiresItSpawns(final TestContext context) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(4, 2, 4));
    vampire.setAiDisabled(true);
    vampire.initialize(
        context.getWorld(),
        context.getWorld().getLocalDifficulty(vampire.getBlockPos()),
        SpawnReason.STRUCTURE,
        new HouseholdArrival(Map.of(ANCIENT, 1), false));
    context.assertEquals(vampire.stageId(), ANCIENT, "the household's only stage is picked");
    context.complete();
  }

  static final String ARENA = "nightborn:vampire_arena";
  static final Identifier FLEDGLING = Identifier.of(Nightborn.MOD_ID, "fledgling");
  static final Identifier ANCIENT = Identifier.of(Nightborn.MOD_ID, "ancient");
  static final Identifier BEAST = Identifier.of(Nightborn.MOD_ID, "beast");
  static final Identifier BEAST_BLOOD = Identifier.of(Nightborn.MOD_ID, "beast/beast_blood");
  static final Identifier POUNCE = Identifier.of(Nightborn.MOD_ID, "beast/pounce");
  static final Identifier BAT_FORM = Identifier.of(Nightborn.MOD_ID, "beast/bat_form");
  static final int GAP_FROM = 3;
  static final int GAP_TO = 5;
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
  public void aVampireMobWeighsUpBloodRushAndWindsUpToCastIt(final TestContext context) {
    final VampireEntity vampire = staged(context, new BlockPos(1, 2, 1), ANCIENT);
    learnBloodRush(context, vampire);
    final VillagerEntity villager = context.spawnEntity(EntityType.VILLAGER, new BlockPos(1, 2, 7));
    villager.setAiDisabled(true);
    vampire.setTarget(villager);
    vampire
        .mobFighting()
        .tick(
            vampire,
            MobFighting.Engagement.FIGHTING,
            vampire.stage().combat().thinkTicks(),
            vampire.stage().combat().bloodFloor());
    final Think think = vampire.mobFighting().lastThink().orElseThrow();
    context.assertEquals(think.intent(), Intent.FIGHT, "a vampire with a target is fighting");
    context.assertEquals(
        think.chosen().map(scored -> scored.option().id()).orElse(null),
        BLOOD_RUSH,
        "it chooses blood rush with its target in range");
    context.assertTrue(vampire.mobFighting().telling(), "it winds up before the cast");
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

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void aVampireMobPouncesOnATargetThatMovesAway(final TestContext context) {
    final VampireEntity vampire = staged(context, new BlockPos(1, 2, 4), ANCIENT);
    SkillAttachment.set(
        vampire,
        SkillTree.VAMPIRE,
        TreeProgress.EMPTY.withEarned(20).buy(BEAST_BLOOD, 1, BEAST).buy(POUNCE, 1, null));
    BloodAttachment.set(vampire, BloodAttachment.FULL);
    final VillagerEntity villager = context.spawnEntity(EntityType.VILLAGER, new BlockPos(4, 2, 4));
    villager.setAiDisabled(true);
    vampire.setTarget(villager);
    final int thinkTicks = vampire.stage().combat().thinkTicks();
    context.runAtTick(
        2,
        () ->
            vampire
                .mobFighting()
                .tick(
                    vampire,
                    MobFighting.Engagement.FIGHTING,
                    thinkTicks,
                    vampire.stage().combat().bloodFloor()));
    context.runAtTick(
        3 + thinkTicks,
        () -> {
          final Vec3d away = context.getAbsolute(Vec3d.ofBottomCenter(new BlockPos(8, 2, 4)));
          villager.refreshPositionAndAngles(away.x, away.y, away.z, 0.0F, 0.0F);
          vampire
              .mobFighting()
              .tick(
                  vampire,
                  MobFighting.Engagement.FIGHTING,
                  thinkTicks,
                  vampire.stage().combat().bloodFloor());
          final Think think = vampire.mobFighting().lastThink().orElseThrow();
          context.assertEquals(
              think.chosen().map(scored -> scored.option().id()).orElse(null),
              POUNCE,
              "it pounces on the target moving away");
          context.assertTrue(
              think.chosen().orElseThrow().score() > 1.5,
              "it weighs the pounce as a runner's, not " + think.chosen().orElseThrow().score());
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 200)
  public void aVampireMobFliesAGapToItsPreyAsABatAndLandsOnTheFarSide(final TestContext context) {
    for (int x = GAP_FROM; x <= GAP_TO; x++) {
      for (int z = 0; z <= 8; z++) {
        for (int y = -1; y <= 1; y++) {
          context.setBlockState(new BlockPos(x, y, z), Blocks.AIR);
        }
      }
    }
    final VampireEntity vampire = staged(context, new BlockPos(1, 2, 4), ANCIENT);
    final DynamicRegistryManager registries = context.getWorld().getRegistryManager();
    SkillAttachment.set(
        vampire,
        SkillTree.VAMPIRE,
        VampireStaging.learned(
            List.of(BEAST_BLOOD, BAT_FORM),
            Skills.allSkills(registries),
            Skills.allBranches(registries)));
    BloodAttachment.set(vampire, BloodAttachment.FULL);
    final VillagerEntity prey = context.spawnEntity(EntityType.VILLAGER, new BlockPos(7, 2, 4));
    prey.setAiDisabled(true);
    final int thinkTicks = vampire.stage().combat().thinkTicks();
    final int floor = vampire.stage().combat().bloodFloor();
    final BatFlightGoal flight = new BatFlightGoal(vampire);
    final AtomicBoolean flown = new AtomicBoolean();
    context.runAtTick(
        3,
        () -> {
          vampire.setTarget(prey);
          final BlockPos edge = context.getAbsolutePos(new BlockPos(GAP_FROM - 1, 2, 4));
          final Path path =
              new Path(
                  List.of(
                      new PathNode(edge.getX() - 1, edge.getY(), edge.getZ()),
                      new PathNode(edge.getX(), edge.getY(), edge.getZ())),
                  prey.getBlockPos(),
                  false);
          vampire.getNavigation().startMovingAlong(path, 1.0);
          vampire.mobFighting().tick(vampire, MobFighting.Engagement.FIGHTING, thinkTicks, floor);
          context.assertEquals(
              vampire
                  .mobFighting()
                  .lastThink()
                  .flatMap(Think::chosen)
                  .map(scored -> scored.option().id())
                  .orElse(null),
              BAT_FORM,
              "it chooses bat form to cross the gap");
        });
    context.runAtTick(
        30,
        () -> {
          vampire.mobFighting().tick(vampire, MobFighting.Engagement.FIGHTING, thinkTicks, floor);
          context.assertTrue(BatForm.isBat(vampire), "it turns into a bat");
          flight.start();
          flown.set(true);
        });
    context.runAtEveryTick(
        () -> {
          if (!flown.get()) {
            return;
          }
          if (BatForm.isBat(vampire)) {
            flight.tick();
            vampire.move(MovementType.SELF, vampire.getVelocity());
            return;
          }
          final Vec3d landed = context.getRelative(vampire.getPos());
          context.assertTrue(
              landed.x >= GAP_TO + 1, "it lands on the far side, not at x " + landed.x);
          context.complete();
        });
  }

  private static VampireEntity staged(
      final TestContext context, final BlockPos pos, final Identifier stage) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, pos);
    vampire.setAiDisabled(true);
    vampire.becomeStage(stage);
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
