package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.combat.CombatMoves;
import com.grahambartley.nightborn.mob.combat.MobFighting;
import com.grahambartley.nightborn.mob.combat.PowerCasting;
import com.grahambartley.nightborn.mob.stage.StageCombat;
import com.grahambartley.nightborn.mob.stage.StageStuck;
import com.grahambartley.nightborn.skill.PowerHeading;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.TreeProgress;
import com.grahambartley.nightborn.vanish.VanishAttachment;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class VanishGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String BATCH = "vanish";
  static final Identifier THICK_BLOOD = Identifier.of("nightborn", "core/thick_blood");
  static final Identifier VANISH = Identifier.of("nightborn", "core/vanish");
  static final BlockPos CENTRE = new BlockPos(4, 2, 4);
  static final double DISTANCE = 4.0;
  static final int GONE_TICKS = 5;
  static final int FULL_BLOOD = 20;
  static final StageCombat ALWAYS_DODGES =
      new StageCombat(0, 20, 0, 0, 0, 100, 0, StageStuck.PLAIN);

  private static void react(final VampireEntity vampire) {
    new MobFighting(new PowerCasting(), new CombatMoves<>(vampire, () -> ALWAYS_DODGES))
        .tick(
            vampire,
            MobFighting.Engagement.FIGHTING,
            ALWAYS_DODGES.thinkTicks(),
            ALWAYS_DODGES.bloodFloor());
  }

  private static void learn(final LivingEntity vampire) {
    SkillAttachment.set(
        vampire,
        SkillTree.VAMPIRE,
        TreeProgress.EMPTY.withEarned(3).buy(THICK_BLOOD, 1, null).buy(VANISH, 2, null));
    BloodAttachment.set(vampire, FULL_BLOOD);
  }

  private static void vanishes(
      final TestContext context, final PowerHeading held, final double east, final double south) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, CENTRE);
    learn(vampire);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final Vec3d start = vampire.getPos();
          vampire.setYaw(0.0F);
          vampire.setPitch(0.0F);
          context.assertEquals(
              Powers.cast(vampire, VANISH, held), PowerUse.Verdict.READY, "the vampire vanished");
          context.assertTrue(
              Math.abs(vampire.getX() - start.x - east) < 0.01
                  && Math.abs(vampire.getZ() - start.z - south) < 0.01,
              "it reappears "
                  + (vampire.getX() - start.x)
                  + " east and "
                  + (vampire.getZ() - start.z)
                  + " south");
          context.assertTrue(Math.abs(vampire.getY() - start.y) < 0.01, "it stays on the floor");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void aPlayerHoldingForwardVanishesAhead(final TestContext context) {
    vanishes(context, PowerHeading.FORWARD, 0.0, DISTANCE);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void aPlayerHoldingBackVanishesBehind(final TestContext context) {
    vanishes(context, PowerHeading.BACK, 0.0, -DISTANCE);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void aPlayerHoldingLeftVanishesToTheLeft(final TestContext context) {
    vanishes(context, PowerHeading.LEFT, DISTANCE, 0.0);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void aPlayerHoldingRightVanishesToTheRight(final TestContext context) {
    vanishes(context, PowerHeading.RIGHT, -DISTANCE, 0.0);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void aPlayerHoldingNothingVanishesBehind(final TestContext context) {
    vanishes(context, PowerHeading.NONE, 0.0, -DISTANCE);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + GONE_TICKS + 10)
  public void aVanishedVampireIsGoneForAMomentAndThenBack(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, CENTRE);
    learn(vampire);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertFalse(VanishAttachment.isGone(vampire), "it is seen before it vanishes");
          Powers.cast(vampire, VANISH, PowerHeading.FORWARD);
          context.assertTrue(VanishAttachment.isGone(vampire), "it is gone as soon as it vanishes");
          context.assertEquals(BloodAttachment.get(vampire), FULL_BLOOD - 1, "it cost one blood");
        });
    context.runAtTick(
        TestPlayers.READY + GONE_TICKS + 2,
        () -> {
          context.assertFalse(VanishAttachment.isGone(vampire), "it is back a moment later");
          TestPlayers.finish(context, vampire);
        });
  }

  private static void stopsShortOf(final TestContext context, final Block floor) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, CENTRE);
    context.setBlockState(new BlockPos(4, 1, 7), floor);
    context.setBlockState(new BlockPos(4, 1, 8), floor);
    learn(vampire);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final Vec3d start = vampire.getPos();
          vampire.setYaw(0.0F);
          vampire.setPitch(0.0F);
          context.assertEquals(
              Powers.cast(vampire, VANISH, PowerHeading.FORWARD),
              PowerUse.Verdict.READY,
              "the vampire vanished");
          final double south = vampire.getZ() - start.z;
          context.assertTrue(south >= 1.99, "it still went forward, by " + south);
          context.assertTrue(south <= 2.2, "it stopped on the last safe floor, at " + south);
          context.setBlockState(new BlockPos(4, 1, 7), Blocks.BEDROCK);
          context.setBlockState(new BlockPos(4, 1, 8), Blocks.BEDROCK);
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void vanishStopsShortOfLava(final TestContext context) {
    stopsShortOf(context, Blocks.LAVA);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void vanishStopsShortOfAMagmaFloor(final TestContext context) {
    stopsShortOf(context, Blocks.MAGMA_BLOCK);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void vanishDoesNotPassThroughAWall(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, CENTRE);
    context.setBlockState(new BlockPos(4, 2, 5), Blocks.STONE);
    context.setBlockState(new BlockPos(4, 3, 5), Blocks.STONE);
    learn(vampire);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final Vec3d start = vampire.getPos();
          vampire.setYaw(0.0F);
          vampire.setPitch(0.0F);
          context.assertEquals(
              Powers.cast(vampire, VANISH, PowerHeading.FORWARD),
              PowerUse.Verdict.NOWHERE,
              "there is nowhere to go");
          context.assertTrue(vampire.getPos().distanceTo(start) < 0.01, "the vampire stays put");
          context.assertEquals(BloodAttachment.get(vampire), FULL_BLOOD, "a refused use is free");
          context.assertFalse(VanishAttachment.isGone(vampire), "and it stays in sight");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void aVampireMobVanishesAsideFromASwing(final TestContext context) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, CENTRE);
    vampire.setAiDisabled(true);
    final ServerPlayerEntity attacker = TestPlayers.human(context, new BlockPos(4, 2, 6));
    learn(vampire);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final Vec3d start = vampire.getPos();
          vampire.setTarget(attacker);
          attacker.handSwinging = true;
          react(vampire);
          context.assertTrue(
              VanishAttachment.isGone(vampire), "the vampire vanished from the swing");
          context.assertTrue(
              vampire.getPos().distanceTo(start) >= 2.0,
              "it moved well away, by " + vampire.getPos().distanceTo(start));
          context.assertTrue(
              vampire.squaredDistanceTo(attacker) > start.squaredDistanceTo(attacker.getPos()),
              "it is farther from the attacker than it was");
          vampire.discard();
          TestPlayers.finish(context, attacker);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void aVampireMobWithoutTheSkillDoesNotVanish(final TestContext context) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, CENTRE);
    vampire.setAiDisabled(true);
    final ServerPlayerEntity attacker = TestPlayers.human(context, new BlockPos(4, 2, 6));
    SkillAttachment.set(vampire, SkillTree.VAMPIRE, TreeProgress.EMPTY);
    BloodAttachment.set(vampire, FULL_BLOOD);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.setTarget(attacker);
          attacker.handSwinging = true;
          react(vampire);
          context.assertFalse(
              VanishAttachment.isGone(vampire), "a vampire without the skill stays in sight");
          vampire.discard();
          TestPlayers.finish(context, attacker);
        });
  }
}
