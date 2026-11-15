package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.vampire.VampireTransitions;
import com.grahambartley.nightborn.weakened.Weakened;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class LevitationGameTest implements FabricGameTest {

  static final String YARD = "nightborn:vampire_yard";
  static final String BATCH = "levitation";
  static final double HOVER = 1.0;
  static final double SETTLED = 0.05;

  @BeforeBatch(batchId = BATCH)
  public void pinMidnight(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
  }

  @AfterBatch(batchId = BATCH)
  public void release(final ServerWorld world) {
    WorldClock.release(world);
  }

  private static VampireEntity still(final TestContext context, final BlockPos at) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, at);
    vampire.setAiDisabled(true);
    return vampire;
  }

  private static ArmorStandEntity stand(final TestContext context, final BlockPos at) {
    final ArmorStandEntity stand = context.spawnEntity(EntityType.ARMOR_STAND, at);
    stand.refreshPositionAndAngles(stand.getX(), stand.getY(), stand.getZ(), 0.0F, 0.0F);
    return stand;
  }

  private static void levitate(final LivingEntity entity) {
    entity.setAttached(Levitation.TYPE, true);
  }

  private static double floorTop(final TestContext context) {
    return context.getAbsolutePos(new BlockPos(0, 1, 0)).getY();
  }

  private static double feet(final TestContext context, final LivingEntity entity) {
    return entity.getY() - floorTop(context);
  }

  private static String motion(final TestContext context, final LivingEntity entity) {
    return "feet "
        + feet(context, entity)
        + ", velocity "
        + entity.getVelocity()
        + ", on ground "
        + entity.isOnGround();
  }

  @GameTest(templateName = YARD, batchId = BATCH, skyAccess = true, tickLimit = 60)
  public void aLevitatingBodyFloatsOneBlockAboveTheGround(final TestContext context) {
    final ArmorStandEntity stand = stand(context, new BlockPos(4, 1, 4));
    context.runAtTick(5, () -> levitate(stand));
    context.runAtTick(
        50,
        () -> {
          context.assertTrue(
              Math.abs(feet(context, stand) - HOVER) < SETTLED,
              "feet one block up: " + motion(context, stand));
          context.complete();
        });
  }

  @GameTest(templateName = YARD, batchId = BATCH, skyAccess = true, tickLimit = 100)
  public void overADropItSinksSlowlyAndNeverBuildsAFall(final TestContext context) {
    final ArmorStandEntity stand = stand(context, new BlockPos(4, 5, 4));
    levitate(stand);
    context.runAtTick(
        10,
        () ->
            context.assertTrue(
                feet(context, stand) > HOVER + 1.5,
                "still sinking slowly: " + motion(context, stand)));
    context.runAtTick(
        90,
        () -> {
          context.assertTrue(
              Math.abs(feet(context, stand) - HOVER) < SETTLED,
              "settled one block up: " + motion(context, stand));
          context.assertTrue(stand.fallDistance < 0.5F, "no fall built up: " + stand.fallDistance);
          context.complete();
        });
  }

  @GameTest(templateName = YARD, batchId = BATCH, skyAccess = true, tickLimit = 20)
  public void aLevitatingVampireCanStillFindAPath(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(1, 2, 1));
    levitate(vampire);
    context.runAtTick(
        5,
        () -> {
          context.assertFalse(vampire.isOnGround(), "held off the ground");
          context.assertTrue(
              vampire.getNavigation().findPathTo(context.getAbsolutePos(new BlockPos(7, 1, 7)), 0)
                  != null,
              "a path while levitating");
          context.complete();
        });
  }

  @GameTest(templateName = YARD, batchId = BATCH, skyAccess = true, tickLimit = 20)
  public void aGroundedVampireOffTheGroundFindsNoPath(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(1, 2, 1));
    context.runAtTick(
        5,
        () -> {
          context.assertFalse(vampire.isOnGround(), "held off the ground");
          context.assertTrue(
              vampire.getNavigation().findPathTo(context.getAbsolutePos(new BlockPos(7, 1, 7)), 0)
                  == null,
              "vanilla finds no path in the air");
          context.complete();
        });
  }

  @GameTest(templateName = YARD, batchId = BATCH, skyAccess = true, tickLimit = 40)
  public void aWeakenedVampireLandsAtOnce(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 1, 4));
    Levitation.start(vampire);
    context.runAtTick(10, () -> Weakened.add(vampire, "garlic", 10, 200));
    context.runAtTick(
        12,
        () -> {
          context.assertFalse(Levitation.isLevitating(vampire), "weakness lands it");
          context.assertFalse(Levitation.start(vampire), "and it cannot start again");
          context.complete();
        });
  }

  @GameTest(templateName = YARD, batchId = BATCH, skyAccess = true, tickLimit = 20)
  public void onlyVampirePlayersLevitate(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 1, 4));
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(5, 1, 4));
    Levitation.request(vampire);
    Levitation.request(human);
    context.assertTrue(Levitation.isLevitating(vampire), "the vampire floats");
    context.assertFalse(Levitation.isLevitating(human), "the human does not");
    Levitation.request(vampire);
    context.assertFalse(Levitation.isLevitating(vampire), "pressing again lands");
    TestPlayers.finish(context, vampire, human);
  }

  @GameTest(templateName = YARD, batchId = BATCH, skyAccess = true, tickLimit = 20)
  public void becomingHumanLands(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 1, 4));
    Levitation.request(vampire);
    VampireTransitions.makeHuman(vampire);
    context.assertFalse(Levitation.isLevitating(vampire), "a human never stays levitating");
    TestPlayers.finish(context, vampire);
  }

  private static void holdAt(
      final TestContext context, final ServerPlayerEntity player, final double relativeFeet) {
    context.runAtEveryTick(
        () -> {
          if (context.getTick() >= TestPlayers.READY) {
            final Vec3d at = context.getAbsolute(new Vec3d(4.5, 0.0, 4.5));
            player.updatePosition(at.x, floorTop(context) + relativeFeet, at.z);
            player.setVelocity(Vec3d.ZERO);
          }
        });
  }

  @GameTest(
      templateName = YARD,
      batchId = BATCH,
      skyAccess = true,
      tickLimit = TestPlayers.READY + 60)
  public void theServerLandsAPlayerHeldAboveTheHover(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 1, 4));
    context.runAtTick(TestPlayers.READY, () -> Levitation.request(vampire));
    holdAt(context, vampire, HOVER + 2.0);
    context.runAtTick(
        TestPlayers.READY + 20,
        () ->
            context.assertTrue(
                Levitation.isLevitating(vampire),
                "one second up is let pass: " + Diagnostics.of(vampire)));
    context.runAtTick(
        TestPlayers.READY + 50,
        () -> {
          context.assertFalse(
              Levitation.isLevitating(vampire),
              "held up too long: "
                  + Diagnostics.of(vampire)
                  + ", guard "
                  + vampire.getAttached(Levitation.GUARD));
          context.assertTrue(
              vampire.hasStatusEffect(StatusEffects.SLOW_FALLING), "set down gently");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(
      templateName = YARD,
      batchId = BATCH,
      skyAccess = true,
      tickLimit = TestPlayers.READY + 70)
  public void aPlayerAtTheHoverKeepsLevitating(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 1, 4));
    context.runAtTick(TestPlayers.READY, () -> Levitation.request(vampire));
    holdAt(context, vampire, HOVER);
    context.runAtTick(
        TestPlayers.READY + 60,
        () -> {
          context.assertTrue(
              Levitation.isLevitating(vampire), "still levitating: " + Diagnostics.of(vampire));
          TestPlayers.finish(context, vampire);
        });
  }
}
