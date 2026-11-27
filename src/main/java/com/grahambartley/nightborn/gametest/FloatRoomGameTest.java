package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.levitation.FloatRoom;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.mob.MobLevitation;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class FloatRoomGameTest implements FabricGameTest {

  static final String YARD = "nightborn:vampire_yard";
  static final String BATCH = "float-room";
  static final int SETTLE_TICKS = 10;
  static final int WALL_X = 6;
  static final int DOOR_Z = 4;
  static final int LINTEL_Y = 4;
  static final float HEIGHT = 1.8F;

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
    vampire.setAppearance(vampire.lookId(), HEIGHT, vampire.width());
    return vampire;
  }

  private static void doorway(final TestContext context) {
    for (int z = 0; z <= 8; z++) {
      for (int y = 2; y <= LINTEL_Y; y++) {
        if (z != DOOR_Z || y == LINTEL_Y) {
          context.setBlockState(new BlockPos(WALL_X, y, z), Blocks.STONE_BRICKS);
        }
      }
    }
  }

  @GameTest(templateName = YARD, batchId = BATCH, skyAccess = true, tickLimit = 40)
  public void aVampireUnderATwoHighCeilingHasNoRoomToFloat(final TestContext context) {
    context.setBlockState(new BlockPos(2, LINTEL_Y, 2), Blocks.STONE_BRICKS);
    final VampireEntity cramped = still(context, new BlockPos(2, 2, 2));
    final VampireEntity open = still(context, new BlockPos(2, 2, 6));
    context.runAtTick(
        SETTLE_TICKS,
        () -> {
          context.assertFalse(FloatRoom.ahead(cramped), "a two-high ceiling leaves no room");
          context.assertTrue(FloatRoom.ahead(open), "the open sky leaves room");
          context.complete();
        });
  }

  @GameTest(templateName = YARD, batchId = BATCH, skyAccess = true, tickLimit = 40)
  public void aFloatingVampireUnderALowCeilingSinksToItsFeet(final TestContext context) {
    context.setBlockState(new BlockPos(2, LINTEL_Y, 2), Blocks.STONE_BRICKS);
    final VampireEntity vampire = still(context, new BlockPos(2, 2, 2));
    context.runAtTick(
        SETTLE_TICKS,
        () -> {
          Levitation.start(vampire);
          MobLevitation.drift(vampire);
          context.assertFalse(Levitation.isLevitating(vampire), "it fights on foot instead");
          context.complete();
        });
  }

  @GameTest(templateName = YARD, batchId = BATCH, skyAccess = true, tickLimit = 40)
  public void aVampireSeesTheLowDoorwayOnItsPathAhead(final TestContext context) {
    doorway(context);
    final VampireEntity near = still(context, new BlockPos(4, 2, DOOR_Z));
    final VampireEntity far = still(context, new BlockPos(1, 2, DOOR_Z));
    final BlockPos beyond = context.getAbsolutePos(new BlockPos(8, 2, DOOR_Z));
    context.runAtTick(
        SETTLE_TICKS,
        () -> {
          for (final VampireEntity vampire : new VampireEntity[] {near, far}) {
            final Path path = vampire.getNavigation().findPathTo(beyond, 0);
            context.assertTrue(path != null, "a path leads through the doorway");
            vampire.getNavigation().startMovingAlong(path, 1.0);
          }
          context.assertFalse(FloatRoom.ahead(near), "the lintel is a few paces ahead");
          context.assertTrue(FloatRoom.ahead(far), "the lintel is still far off");
          context.complete();
        });
  }
}
