package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LongDoorInteractGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class VampireDoorGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final int WIDTH = 9;
  static final int START = 5;
  static final BlockPos DOOR = new BlockPos(4, 2, 4);
  static final BlockPos NEAR = new BlockPos(4, 2, 3);
  static final BlockPos FAR = new BlockPos(4, 2, 6);
  static final Vec3d STEP = new Vec3d(0.0, 0.0, 0.15);

  private static void wallWithDoor(final TestContext context, final Block door) {
    for (int x = 0; x < WIDTH; x++) {
      if (x != DOOR.getX()) {
        context.setBlockState(x, DOOR.getY(), DOOR.getZ(), Blocks.STONE_BRICKS);
        context.setBlockState(x, DOOR.getY() + 1, DOOR.getZ(), Blocks.STONE_BRICKS);
      }
    }
    final BlockState lower = door.getDefaultState().with(DoorBlock.FACING, Direction.SOUTH);
    context.setBlockState(DOOR, lower);
    context.setBlockState(DOOR.up(), lower.with(DoorBlock.HALF, DoubleBlockHalf.UPPER));
  }

  private static VampireEntity vampire(final TestContext context) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, NEAR);
    vampire.setAiDisabled(true);
    return vampire;
  }

  private static Goal doorGoal(final VampireEntity vampire) {
    return vampire.goalSelector.getGoals().stream()
        .map(PrioritizedGoal::getGoal)
        .filter(LongDoorInteractGoal.class::isInstance)
        .findFirst()
        .orElseThrow();
  }

  private static boolean open(final TestContext context) {
    return context.getBlockState(DOOR).get(DoorBlock.OPEN);
  }

  private static Path pathAcross(final TestContext context, final VampireEntity vampire) {
    return vampire.getNavigation().findPathTo(context.getAbsolutePos(FAR), 0);
  }

  private static boolean walkInto(final VampireEntity vampire, final Path path, final Goal goal) {
    vampire.getNavigation().startMovingAlong(path, 1.0);
    vampire.move(MovementType.SELF, new Vec3d(0.0, 0.0, 1.0));
    return goal.canStart();
  }

  @GameTest(templateName = ARENA, tickLimit = 60)
  public void aVampireOpensAnOakDoorWalksThroughAndClosesIt(final TestContext context) {
    wallWithDoor(context, Blocks.OAK_DOOR);
    final VampireEntity vampire = vampire(context);
    final Goal goal = doorGoal(vampire);
    final double doorway = context.getAbsolutePos(DOOR).getZ() + 0.5;
    final AtomicBoolean walking = new AtomicBoolean();
    context.runAtTick(
        START,
        () -> {
          final Path path = pathAcross(context, vampire);
          context.assertTrue(path != null && path.reachesTarget(), "a path leads through the door");
          context.assertTrue(walkInto(vampire, path, goal), "a shut oak door starts the door goal");
          goal.start();
          context.assertTrue(open(context), "the vampire opened the door");
          walking.set(true);
        });
    context.runAtEveryTick(
        () -> {
          if (!walking.get()) {
            return;
          }
          vampire.move(MovementType.SELF, STEP);
          if (goal.shouldContinue()) {
            goal.tick();
            return;
          }
          goal.stop();
          context.assertTrue(vampire.getZ() > doorway, "the vampire is through the doorway");
          context.assertFalse(open(context), "the vampire closed the door behind it");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aVampireLeavesAnIronDoorShut(final TestContext context) {
    wallWithDoor(context, Blocks.IRON_DOOR);
    final VampireEntity vampire = vampire(context);
    final Goal goal = doorGoal(vampire);
    context.runAtTick(
        START,
        () -> {
          final Path path = pathAcross(context, vampire);
          context.assertFalse(
              path != null && path.reachesTarget(), "no path leads through an iron door");
          context.assertFalse(walkInto(vampire, path, goal), "an iron door never starts the goal");
          context.assertFalse(open(context), "the iron door stays shut");
          context.complete();
        });
  }
}
