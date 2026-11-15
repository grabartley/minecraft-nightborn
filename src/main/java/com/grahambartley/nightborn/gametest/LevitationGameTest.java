package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.config.MobSettings;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.VampireTransitions;
import com.grahambartley.nightborn.weakened.Weakened;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;

public class LevitationGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String BATCH = "levitation";
  static final int MIDNIGHT = 18000;
  static final double FLOOR = 1.0;
  static final double HOVER = FLOOR + 1.0;

  private static long timeBefore;
  private static boolean cycleBefore;
  private static int riseBefore;

  @BeforeBatch(batchId = BATCH)
  public void pinMidnightAndStillTheMobs(final ServerWorld world) {
    timeBefore = world.getTimeOfDay();
    cycleBefore = world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE);
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, world.getServer());
    world.setTimeOfDay(MIDNIGHT);
    riseBefore = ServerConfigService.current().mobs().vampireLevitateSeconds();
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current().with(MobSettings.VAMPIRE_LEVITATE_SECONDS, 0));
  }

  @AfterBatch(batchId = BATCH)
  public void release(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(cycleBefore, world.getServer());
    world.setTimeOfDay(timeBefore);
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current().with(MobSettings.VAMPIRE_LEVITATE_SECONDS, riseBefore));
  }

  private static VampireEntity still(final TestContext context, final BlockPos at) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, at);
    vampire.setAiDisabled(true);
    return vampire;
  }

  private static double feet(final TestContext context, final LivingEntity entity) {
    return context.getRelative(entity.getPos()).y;
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 60)
  public void aLevitatingVampireFloatsOneBlockAboveTheGround(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 1, 4));
    context.runAtTick(5, () -> context.assertTrue(Levitation.start(vampire), "it may levitate"));
    context.runAtTick(
        50,
        () -> {
          context.assertTrue(
              Math.abs(feet(context, vampire) - HOVER) < 0.05,
              "feet one block up, at " + feet(context, vampire));
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 90)
  public void overADropItSinksSlowlyAndTakesNoFallDamage(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 3, 4));
    Levitation.start(vampire);
    final float health = vampire.getHealth();
    context.runAtTick(
        10,
        () ->
            context.assertTrue(
                feet(context, vampire) > HOVER + 0.4,
                "still sinking slowly, at " + feet(context, vampire)));
    context.runAtTick(
        80,
        () -> {
          context.assertTrue(
              Math.abs(feet(context, vampire) - HOVER) < 0.05, "settled one block up");
          context.assertEquals(vampire.getHealth(), health, "no fall damage");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 60)
  public void aLevitatingVampireCanStillFindAPath(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(1, 1, 1));
    context.runAtTick(5, () -> Levitation.start(vampire));
    context.runAtTick(
        40,
        () -> {
          context.assertFalse(vampire.isOnGround(), "off the ground");
          context.assertTrue(
              vampire.getNavigation().findPathTo(context.getAbsolutePos(new BlockPos(7, 1, 7)), 0)
                  != null,
              "a path while levitating");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 40)
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

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
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

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
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
            final Vec3d at = context.getAbsolute(new Vec3d(4.5, relativeFeet, 4.5));
            player.updatePosition(at.x, at.y, at.z);
            player.setVelocity(Vec3d.ZERO);
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 60)
  public void theServerLandsAPlayerHeldAboveTheHover(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 1, 4));
    context.runAtTick(TestPlayers.READY, () -> Levitation.request(vampire));
    holdAt(context, vampire, HOVER + 1.0);
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
              Levitation.isLevitating(vampire), "held up too long: " + Diagnostics.of(vampire));
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 70)
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

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 60)
  public void levitatingKeepsWalkingPace(final TestContext context) {
    final ArmorStandEntity walker = stand(context, new BlockPos(2, 1, 1));
    final ArmorStandEntity floater = stand(context, new BlockPos(6, 1, 1));
    context.runAtTick(5, () -> floater.setAttached(Levitation.TYPE, true));
    context.runAtEveryTick(
        () -> {
          if (context.getTick() >= 25) {
            walker.forwardSpeed = 1.0F;
            floater.forwardSpeed = 1.0F;
            walker.setMovementSpeed(0.1F);
            floater.setMovementSpeed(0.1F);
          }
        });
    final Vec3d[] start = new Vec3d[2];
    context.runAtTick(
        30,
        () -> {
          start[0] = walker.getPos();
          start[1] = floater.getPos();
        });
    context.runAtTick(
        45,
        () -> {
          final double walked = walker.getPos().subtract(start[0]).horizontalLength();
          final double floated = floater.getPos().subtract(start[1]).horizontalLength();
          context.assertTrue(walked > 0.5, "the walker moved " + walked);
          context.assertTrue(
              feet(context, floater) > FLOOR + 0.5,
              "the floater is up, at " + feet(context, floater));
          context.assertTrue(
              Math.abs(floated - walked) < walked * 0.15,
              "floated " + floated + " against walked " + walked);
          context.complete();
        });
  }

  private static ArmorStandEntity stand(final TestContext context, final BlockPos at) {
    final ArmorStandEntity stand = context.spawnEntity(EntityType.ARMOR_STAND, at);
    stand.refreshPositionAndAngles(stand.getX(), stand.getY(), stand.getZ(), 0.0F, 0.0F);
    return stand;
  }
}
