package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.coffin.CoffinBlock;
import com.grahambartley.nightborn.coffin.CoffinDayCycle;
import com.grahambartley.nightborn.coffin.CoffinWood;
import com.grahambartley.nightborn.coffin.Coffins;
import com.grahambartley.nightborn.core.NightTime;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.SleepManager;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameRules;
import net.minecraft.world.TeleportTarget;

public class CoffinGameTest implements FabricGameTest {

  static final String DAY = "coffin-day";
  static final String SKIP = "coffin-skip";
  static final String NIGHT = "coffin-night";
  static final String BED_SHARE = "coffin-bed-share";
  static final String YARD = "nightborn:vampire_yard";
  static final int NOON = 6000;
  static final int MIDNIGHT = 18000;
  static final int SLEEP_READY = 105;

  @BeforeBatch(batchId = DAY)
  public void pinNoon(final ServerWorld world) {
    pin(world, NOON, false);
  }

  @AfterBatch(batchId = DAY)
  public void releaseNoon(final ServerWorld world) {
    release(world);
  }

  @BeforeBatch(batchId = SKIP)
  public void startAtNoon(final ServerWorld world) {
    pin(world, NOON, true);
  }

  @AfterBatch(batchId = SKIP)
  public void releaseSkip(final ServerWorld world) {
    release(world);
  }

  @BeforeBatch(batchId = NIGHT)
  public void pinMidnight(final ServerWorld world) {
    pin(world, MIDNIGHT, false);
  }

  @AfterBatch(batchId = NIGHT)
  public void releaseMidnight(final ServerWorld world) {
    release(world);
  }

  @BeforeBatch(batchId = BED_SHARE)
  public void startAtMidnight(final ServerWorld world) {
    pin(world, MIDNIGHT, false);
  }

  @AfterBatch(batchId = BED_SHARE)
  public void releaseBedShare(final ServerWorld world) {
    release(world);
  }

  @GameTest(
      templateName = YARD,
      batchId = SKIP,
      tickLimit = TestPlayers.READY + 2 * SLEEP_READY + 40)
  public void theDayPassesOnlyWhenEveryVampireRests(final TestContext context) {
    final BlockPos first = coffin(context, new BlockPos(2, 2, 6), Direction.NORTH);
    final BlockPos second = coffin(context, new BlockPos(6, 2, 6), Direction.NORTH);
    roof(context);
    final ServerPlayerEntity a = TestPlayers.vampire(context, new BlockPos(3, 2, 5));
    final ServerPlayerEntity b = TestPlayers.vampire(context, new BlockPos(5, 2, 5));
    final List<ServerPlayerEntity> both = List.of(a, b);
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
          context.assertTrue(a.isSleeping(), "the first vampire rests: " + Diagnostics.of(a));
          CoffinDayCycle.settle(context.getWorld(), both, 100);
          context.assertTrue(
              context.getWorld().isDay() && a.isSleeping(),
              "one of two vampires resting does not pass the day");
          context.useBlock(second, b);
        });
    context.runAtTick(
        TestPlayers.READY + 2 * SLEEP_READY + 10,
        () -> {
          if (b.isSleeping()) {
            CoffinDayCycle.settle(context.getWorld(), both, 100);
          }
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
      templateName = YARD,
      batchId = DAY,
      skyAccess = true,
      tickLimit = TestPlayers.READY + 80)
  public void aShutCoffinShadesItsSleeperFromTheSun(final TestContext context) {
    final BlockPos head = coffin(context, new BlockPos(4, 2, 6), Direction.NORTH);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(5, 2, 5));
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
              vampire.getHealth() >= settled[0] && !vampire.isOnFire(),
              "the sun does not reach a closed coffin: " + Diagnostics.of(vampire));
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = YARD, batchId = DAY, tickLimit = TestPlayers.READY + 10)
  public void restingSetsTheRespawnPointAndTheCoffinsLossClearsIt(final TestContext context) {
    final BlockPos head = coffin(context, new BlockPos(4, 2, 6), Direction.EAST);
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

  @GameTest(templateName = YARD, batchId = DAY, tickLimit = TestPlayers.READY + 10)
  public void aSecondVampireIsRefused(final TestContext context) {
    final BlockPos head = coffin(context, new BlockPos(4, 2, 6), Direction.NORTH);
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

  @GameTest(templateName = YARD, batchId = DAY, tickLimit = TestPlayers.READY + 10)
  public void theLidShutsOverTheSleeperAndOpensWhenTheyRise(final TestContext context) {
    final BlockPos head = coffin(context, new BlockPos(4, 2, 6), Direction.SOUTH);
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

  @GameTest(templateName = YARD, batchId = DAY, tickLimit = TestPlayers.READY + 10)
  public void aHumanOpensACoffinButCannotRestInIt(final TestContext context) {
    final BlockPos head = coffin(context, new BlockPos(4, 2, 6), Direction.NORTH);
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

  @GameTest(templateName = YARD, batchId = DAY, tickLimit = TestPlayers.READY + 10)
  public void aVampireCannotSleepInABed(final TestContext context) {
    final BlockPos head = bed(context, new BlockPos(4, 2, 6), Direction.NORTH);
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

  @GameTest(templateName = YARD, batchId = DAY, tickLimit = TestPlayers.READY + 10)
  public void breakingEitherHalfDropsOneCoffin(final TestContext context) {
    final BlockPos head = coffin(context, new BlockPos(4, 2, 6), Direction.WEST);
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

  @GameTest(templateName = YARD, batchId = NIGHT, tickLimit = TestPlayers.READY + 10)
  public void aVampireCannotRestAtNight(final TestContext context) {
    final BlockPos head = coffin(context, new BlockPos(4, 2, 6), Direction.NORTH);
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
      templateName = YARD,
      batchId = BED_SHARE,
      tickLimit = TestPlayers.READY + SLEEP_READY + 10)
  public void anAwakeVampireNeverHoldsBackTheNight(final TestContext context) {
    final BlockPos head = bed(context, new BlockPos(4, 2, 6), Direction.NORTH);
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

  private static void roof(final TestContext context) {
    for (int x = 0; x <= 8; x++) {
      for (int z = 0; z <= 8; z++) {
        context.setBlockState(new BlockPos(x, 4, z), Blocks.STONE);
      }
    }
  }

  private static BlockPos coffin(
      final TestContext context, final BlockPos foot, final Direction facing) {
    return pair(context, Coffins.get(CoffinWood.DARK_OAK), foot, facing);
  }

  private static BlockPos bed(
      final TestContext context, final BlockPos foot, final Direction facing) {
    return pair(context, Blocks.RED_BED, foot, facing);
  }

  private static BlockPos pair(
      final TestContext context, final Block block, final BlockPos foot, final Direction facing) {
    final BlockState footState =
        block.getDefaultState().with(BedBlock.FACING, facing).with(BedBlock.PART, BedPart.FOOT);
    final BlockPos head = foot.offset(facing);
    context.setBlockState(foot, footState);
    context.setBlockState(head, footState.with(BedBlock.PART, BedPart.HEAD));
    return head;
  }

  private static void pin(final ServerWorld world, final int time, final boolean cycle) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(cycle, world.getServer());
    world.setTimeOfDay(time);
    world.setWeather(12000, 0, false, false);
  }

  private static void release(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, world.getServer());
  }
}
