package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.coffin.CoffinBlock;
import com.grahambartley.nightborn.coffin.CoffinLayout;
import com.grahambartley.nightborn.coffin.CoffinRise;
import com.grahambartley.nightborn.coffin.RiseRules;
import com.grahambartley.nightborn.config.CoffinSettings;
import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.SleepManager;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

public class CoffinGameTest implements FabricGameTest {

  static final String DAY = "coffin-day";
  static final String SKIP = "coffin-skip";
  static final String HALF_SHARE = "coffin-half-share";
  static final String NIGHT = "coffin-night";
  static final String BED_SHARE = "coffin-bed-share";
  static final String STORM = "coffin-storm";
  static final String DUSK = "coffin-dusk";
  static final int SLEEP_READY = 105;
  static final double BOUNCE = 0.05;
  private static int shareBefore;

  @BeforeBatch(batchId = DAY)
  public void pinNoon(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.NOON);
  }

  @AfterBatch(batchId = DAY)
  public void releaseNoon(final ServerWorld world) {
    WorldClock.release(world);
  }

  @BeforeBatch(batchId = SKIP)
  public void startAtNoon(final ServerWorld world) {
    WorldClock.runFrom(world, WorldClock.NOON);
    TestPlayers.disconnectEveryone(world.getServer());
  }

  @AfterBatch(batchId = SKIP)
  public void releaseSkip(final ServerWorld world) {
    WorldClock.release(world);
  }

  @BeforeBatch(batchId = HALF_SHARE)
  public void halveTheShare(final ServerWorld world) {
    WorldClock.runFrom(world, WorldClock.NOON);
    TestPlayers.disconnectEveryone(world.getServer());
    shareBefore = ServerConfigService.current().coffins().sleepShare();
    ServerConfigService.update(
        world.getServer(), ServerConfigService.current().with(CoffinSettings.SLEEP_SHARE, 50));
  }

  @AfterBatch(batchId = HALF_SHARE)
  public void restoreTheShare(final ServerWorld world) {
    WorldClock.release(world);
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current().with(CoffinSettings.SLEEP_SHARE, shareBefore));
  }

  @BeforeBatch(batchId = NIGHT)
  public void pinMidnight(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
  }

  @AfterBatch(batchId = NIGHT)
  public void releaseMidnight(final ServerWorld world) {
    WorldClock.release(world);
  }

  @BeforeBatch(batchId = BED_SHARE)
  public void startAtMidnight(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
  }

  @AfterBatch(batchId = BED_SHARE)
  public void releaseBedShare(final ServerWorld world) {
    WorldClock.release(world);
  }

  @BeforeBatch(batchId = STORM)
  public void brewAStormAtNoon(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.NOON);
    world.setWeather(0, 12000, true, true);
    world.setRainGradient(1.0F);
    world.setThunderGradient(1.0F);
  }

  @AfterBatch(batchId = STORM)
  public void clearTheStorm(final ServerWorld world) {
    world.setWeather(12000, 0, false, false);
    world.setRainGradient(0.0F);
    world.setThunderGradient(0.0F);
    WorldClock.release(world);
  }

  @BeforeBatch(batchId = DUSK)
  public void letTheDayRun(final ServerWorld world) {
    WorldClock.runFrom(world, WorldClock.NOON);
  }

  @AfterBatch(batchId = DUSK)
  public void releaseDusk(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = STORM, tickLimit = TestPlayers.READY + 50)
  public void aThunderstormAtNoonIsStillTheDayForACoffin(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 6), Direction.NORTH);
    CoffinStage.roof(context);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(5, 2, 5));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertFalse(context.getWorld().isDay(), "a thunderstorm darkens the sky at noon");
          vampire.extinguish();
          context.useBlock(head, vampire);
          context.assertTrue(vampire.isSleeping(), "a vampire lies down under a stormy noon");
        });
    context.runAtTick(
        TestPlayers.READY + 40,
        () -> {
          context.assertTrue(
              vampire.isSleeping(),
              "the storm never wakes the sleeper: " + Diagnostics.of(vampire));
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(
      templateName = CoffinStage.YARD,
      batchId = DUSK,
      tickLimit = TestPlayers.READY + RiseRules.RISE_TICKS + 60)
  public void aRestingVampireRisesAtDusk(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 6), Direction.NORTH);
    CoffinStage.roof(context);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(5, 2, 5));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.getWorld().setTimeOfDay(NightTime.DUSK - 20);
          vampire.extinguish();
          context.useBlock(head, vampire);
          context.assertTrue(vampire.isSleeping(), "a vampire lies down just before dusk");
        });
    context.runAtTick(
        TestPlayers.READY + RiseRules.RISE_TICKS + 40,
        () -> {
          final long time = Math.floorMod(context.getWorld().getTimeOfDay(), NightTime.DAY_LENGTH);
          context.assertTrue(time >= NightTime.DUSK, "dusk has come, time " + time);
          context.assertFalse(vampire.isSleeping(), "the vampire rises at dusk");
          context.assertTrue(
              context.getBlockState(head).get(CoffinBlock.OPEN), "the lid opens as they rise");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(
      templateName = CoffinStage.YARD,
      batchId = SKIP,
      tickLimit = TestPlayers.READY + 2 * SLEEP_READY + RiseRules.RISE_TICKS + 20)
  public void theDayPassesOnlyWhenEveryVampireRests(final TestContext context) {
    final BlockPos first = CoffinStage.coffin(context, new BlockPos(2, 2, 6), Direction.NORTH);
    final BlockPos second = CoffinStage.coffin(context, new BlockPos(6, 2, 6), Direction.NORTH);
    CoffinStage.roof(context);
    final ServerPlayerEntity a = TestPlayers.vampire(context, new BlockPos(3, 2, 5));
    final ServerPlayerEntity b = TestPlayers.vampire(context, new BlockPos(5, 2, 5));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          a.extinguish();
          b.extinguish();
          context.useBlock(first, a);
        });
    context.runAtTick(
        TestPlayers.READY + SLEEP_READY,
        () -> {
          context.assertTrue(
              a.isSleeping() && context.getWorld().isDay(),
              "one of two vampires resting does not pass the day: " + Diagnostics.of(a));
          context.useBlock(second, b);
        });
    context.runAtTick(
        TestPlayers.READY + 2 * SLEEP_READY + RiseRules.RISE_TICKS + 10,
        () -> {
          final long time = Math.floorMod(context.getWorld().getTimeOfDay(), NightTime.DAY_LENGTH);
          context.assertTrue(
              time >= NightTime.DUSK && time < NightTime.DUSK + SLEEP_READY,
              "both vampires resting pass the day to dusk, time " + time);
          context.assertFalse(a.isSleeping() || b.isSleeping(), "both rise at dusk");
          context.assertTrue(
              context.getBlockState(first).get(CoffinBlock.OPEN), "the lid opens as they rise");
          TestPlayers.finish(context, a, b);
        });
  }

  @GameTest(
      templateName = CoffinStage.YARD,
      batchId = HALF_SHARE,
      tickLimit = TestPlayers.READY + SLEEP_READY + 20)
  public void theSleepShareSettingLetsHalfTheVampiresPassTheDay(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(2, 2, 6), Direction.NORTH);
    CoffinStage.roof(context);
    final ServerPlayerEntity resting = TestPlayers.vampire(context, new BlockPos(3, 2, 5));
    final ServerPlayerEntity awake = TestPlayers.vampire(context, new BlockPos(6, 2, 3));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          resting.extinguish();
          awake.extinguish();
          context.useBlock(head, resting);
        });
    context.runAtTick(
        TestPlayers.READY + SLEEP_READY + 10,
        () -> {
          final long time = Math.floorMod(context.getWorld().getTimeOfDay(), NightTime.DAY_LENGTH);
          context.assertTrue(
              time >= NightTime.DUSK && time < NightTime.DUSK + SLEEP_READY,
              "one of two vampires resting passes the day at a 50% share, time " + time);
          TestPlayers.finish(context, resting, awake);
        });
  }

  @GameTest(
      templateName = CoffinStage.YARD,
      batchId = DAY,
      skyAccess = true,
      tickLimit = TestPlayers.READY + 80)
  public void aShutCoffinShadesItsSleeperFromTheSun(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 6), Direction.NORTH);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(5, 2, 5));
    final ServerPlayerEntity exposed = TestPlayers.vampire(context, new BlockPos(1, 2, 2));
    final float[] settled = new float[1];
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.useBlock(head, vampire);
          vampire.extinguish();
          settled[0] = vampire.getHealth();
        });
    context.runAtTick(
        TestPlayers.READY + 60,
        () -> {
          context.assertTrue(vampire.isSleeping(), "the vampire is still resting");
          context.assertFalse(
              context.getBlockState(head).get(CoffinBlock.OPEN), "the lid shut over the sleeper");
          context.assertTrue(
              exposed.isOnFire(),
              "the sun burns a vampire standing in the open: " + Diagnostics.of(exposed));
          context.assertTrue(
              vampire.getHealth() >= settled[0] && !vampire.isOnFire(),
              "the sun does not reach a closed coffin: " + Diagnostics.of(vampire));
          TestPlayers.finish(context, vampire, exposed);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = DAY, tickLimit = TestPlayers.READY + 50)
  public void aBurningVampireLiesDownAndTheFlamesGoOut(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 6), Direction.NORTH);
    CoffinStage.roof(context);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(5, 2, 5));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.setOnFireFor(8.0F);
          context.useBlock(head, vampire);
          context.assertFalse(vampire.isOnFire(), "a shut coffin smothers the flames");
        });
    context.runAtTick(
        TestPlayers.READY + 40,
        () -> {
          context.assertTrue(
              vampire.isSleeping(),
              "the flames never wake the sleeper: " + Diagnostics.of(vampire));
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = DAY, tickLimit = TestPlayers.READY + 10)
  public void restingSetsTheRespawnPointAndTheCoffinsLossClearsIt(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 6), Direction.EAST);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.useBlock(head, vampire);
          context.assertTrue(
              context.getAbsolutePos(head).equals(vampire.getSpawnPointPosition()),
              "lying in a coffin sets the respawn point there");
          context.assertFalse(
              vampire.getRespawnTarget(false, TeleportTarget.NO_OP).missingRespawnBlock(),
              "a standing coffin is a valid respawn point");
          vampire.wakeUp(false, true);
          context.getWorld().breakBlock(context.getAbsolutePos(head), false);
          context.assertTrue(
              vampire.getRespawnTarget(false, TeleportTarget.NO_OP).missingRespawnBlock(),
              "a vampire whose coffin is gone respawns at world spawn");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = DAY, tickLimit = TestPlayers.READY + 10)
  public void aSecondVampireIsRefused(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 6), Direction.NORTH);
    final ServerPlayerEntity first = TestPlayers.vampire(context, new BlockPos(3, 2, 5));
    final ServerPlayerEntity second = TestPlayers.vampire(context, new BlockPos(5, 2, 5));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.useBlock(head, first);
          context.useBlock(head, second);
          context.assertTrue(first.isSleeping(), "the first vampire lies down");
          context.assertFalse(second.isSleeping(), "the second vampire is refused");
          context.assertFalse(
              context.getBlockState(head).get(CoffinBlock.OPEN), "the refusal leaves the lid shut");
          TestPlayers.finish(context, first, second);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = DAY, tickLimit = TestPlayers.READY + 10)
  public void theLidShutsOverTheSleeperAndOpensWhenTheyRise(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 6), Direction.SOUTH);
    final BlockPos foot = head.offset(Direction.NORTH);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 6));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.useBlock(foot, vampire);
          context.assertTrue(vampire.isSleeping(), "using the foot lies in the coffin too");
          context.assertFalse(context.getBlockState(head).get(CoffinBlock.OPEN), "head shut");
          context.assertFalse(context.getBlockState(foot).get(CoffinBlock.OPEN), "foot shut");
          vampire.wakeUp(false, true);
          context.assertTrue(context.getBlockState(head).get(CoffinBlock.OPEN), "head open");
          context.assertTrue(context.getBlockState(foot).get(CoffinBlock.OPEN), "foot open");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(
      templateName = CoffinStage.YARD,
      batchId = DAY,
      tickLimit = TestPlayers.READY + RiseRules.RISE_TICKS + 10)
  public void aWakingVampireRisesFromItsHeelsThroughDamageThenStandsClear(
      final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 6), Direction.SOUTH);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 6));
    final Vec3d heels =
        CoffinLayout.heels(
            context.getAbsolutePos(head),
            Direction.SOUTH,
            vampire.getEyeHeight(EntityPose.STANDING));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.useBlock(head, vampire);
          vampire.wakeUp(false, true);
          context.assertTrue(
              CoffinRise.rising(vampire) && vampire.isSleeping(),
              "waking starts the rise with the vampire still held in the coffin");
          context.assertTrue(
              vampire.getPose() == EntityPose.STANDING, "the rise stands the body up");
        });
    context.runAtTick(
        TestPlayers.READY + 5,
        () -> {
          vampire.damage(context.getWorld().getDamageSources().generic(), 1.0F);
          context.assertTrue(
              CoffinRise.rising(vampire) && vampire.isSleeping(), "damage does not end the rise");
          context.assertTrue(
              vampire.getPos().squaredDistanceTo(heels) < 1.0E-6,
              "the vampire pivots on its heels at " + heels + ", not " + vampire.getPos());
        });
    context.runAtTick(
        TestPlayers.READY + RiseRules.RISE_TICKS + 2,
        () -> {
          context.assertTrue(
              !vampire.isSleeping() && !CoffinRise.rising(vampire),
              "the vampire is awake once the rise ends");
          context.assertTrue(
              context.getWorld().isSpaceEmpty(vampire),
              "the vampire stands clear of the coffin, at " + vampire.getPos());
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = DAY, tickLimit = TestPlayers.READY + 10)
  public void aSneakingVampireWorksTheLidInsteadOfLyingDown(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 6), Direction.NORTH);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(5, 2, 5));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.setSneaking(true);
          context.useBlock(head, vampire);
          context.assertTrue(context.getBlockState(head).get(CoffinBlock.OPEN), "the lid opens");
          context.assertFalse(vampire.isSleeping(), "a sneaking vampire does not lie down");
          context.useBlock(head, vampire);
          context.assertFalse(context.getBlockState(head).get(CoffinBlock.OPEN), "the lid shuts");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = DAY, tickLimit = TestPlayers.READY + 10)
  public void aHumanOpensACoffinButCannotRestInIt(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 6), Direction.NORTH);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(5, 2, 5));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.useBlock(head, human);
          context.assertTrue(context.getBlockState(head).get(CoffinBlock.OPEN), "the lid opens");
          context.assertTrue(
              human.trySleep(context.getAbsolutePos(head)).left().isPresent(),
              "a human is refused even when trying to lie down directly");
          context.assertFalse(human.isSleeping(), "a human never rests in a coffin");
          context.useBlock(head, human);
          context.assertFalse(context.getBlockState(head).get(CoffinBlock.OPEN), "the lid shuts");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = DAY, tickLimit = TestPlayers.READY + 10)
  public void aVampireCannotSleepInABed(final TestContext context) {
    final BlockPos head = CoffinStage.bed(context, new BlockPos(4, 2, 6), Direction.NORTH);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(5, 2, 5));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertTrue(
              vampire.trySleep(context.getAbsolutePos(head)).left().isPresent(),
              "a vampire is refused a bed");
          context.assertFalse(vampire.isSleeping(), "a vampire never sleeps in a bed");
          context.assertTrue(
              vampire.getSpawnPointPosition() == null, "a refused bed sets no respawn point");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = DAY, tickLimit = TestPlayers.READY + 10)
  public void breakingEitherHalfDropsOneCoffin(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 6), Direction.WEST);
    final BlockPos foot = head.offset(Direction.EAST);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.getWorld().breakBlock(context.getAbsolutePos(foot), true);
          context.expectBlock(Blocks.AIR, head);
          context.expectBlock(Blocks.AIR, foot);
          final int dropped =
              context
                  .getWorld()
                  .getEntitiesByClass(ItemEntity.class, context.getTestBox(), item -> true)
                  .stream()
                  .mapToInt(item -> item.getStack().getCount())
                  .sum();
          context.assertTrue(dropped == 1, "breaking a coffin drops one coffin, got " + dropped);
          context.complete();
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = DAY, tickLimit = 60)
  public void anItemDroppedOnACoffinLandsWithoutTheBounceABedGives(final TestContext context) {
    final BlockPos coffinHead = CoffinStage.coffin(context, new BlockPos(2, 2, 6), Direction.NORTH);
    final BlockPos bedHead = CoffinStage.bed(context, new BlockPos(6, 2, 6), Direction.NORTH);
    final ItemEntity onCoffin = context.spawnItem(Items.STICK, 2.5F, 3.5F, 5.5F);
    final ItemEntity onBed = context.spawnItem(Items.STICK, 6.5F, 3.5F, 5.5F);
    for (final ItemEntity item : List.of(onCoffin, onBed)) {
      item.setVelocity(Vec3d.ZERO);
      item.setPickupDelayInfinite();
    }
    final boolean[] bounced = new boolean[2];
    context.runAtEveryTick(
        () -> {
          bounced[0] |= onCoffin.getVelocity().y > BOUNCE;
          bounced[1] |= onBed.getVelocity().y > BOUNCE;
        });
    context.runAtTick(
        50,
        () -> {
          context.assertTrue(bounced[1], "an item bounces off a bed");
          context.assertFalse(bounced[0], "an item does not bounce off a coffin");
          context.assertTrue(
              onCoffin.getY() > context.getAbsolutePos(coffinHead).getY() + 0.5,
              "the item rests on the coffin's lid, at y " + onCoffin.getY());
          context.expectBlock(Blocks.RED_BED, bedHead);
          context.complete();
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = NIGHT, tickLimit = TestPlayers.READY + 10)
  public void aVampireCannotRestAtNight(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 6), Direction.NORTH);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(5, 2, 5));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.useBlock(head, vampire);
          context.assertFalse(vampire.isSleeping(), "a coffin is no rest at night");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(
      templateName = CoffinStage.YARD,
      batchId = BED_SHARE,
      tickLimit = TestPlayers.READY + SLEEP_READY + 10)
  public void anAwakeVampireNeverHoldsBackTheNight(final TestContext context) {
    final BlockPos head = CoffinStage.bed(context, new BlockPos(4, 2, 6), Direction.NORTH);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(5, 2, 5));
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 2));
    context.runAtTick(TestPlayers.READY, () -> context.useBlock(head, human));
    context.runAtTick(
        TestPlayers.READY + SLEEP_READY,
        () -> {
          context.assertTrue(human.isSleeping(), "the human sleeps: " + Diagnostics.of(human));
          final List<ServerPlayerEntity> both = List.of(human, vampire);
          final SleepManager beds = new SleepManager();
          beds.update(both);
          context.assertTrue(
              beds.canSkipNight(100) && beds.canResetTime(100, both),
              "the only human asleep passes the night though a vampire is awake");
          TestPlayers.finish(context, human, vampire);
        });
  }
}
