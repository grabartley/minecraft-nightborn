package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.math.BlockPos;

public class RouteHeadroomGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final int WIDTH = 9;
  static final int FLOOR = 2;
  static final int WALL_Z = 4;
  static final int GAP_X = 4;
  static final int WALL_TOP = FLOOR + 5;
  static final int ROOF = 4;
  static final int START = 5;
  static final BlockPos NEAR = new BlockPos(GAP_X, FLOOR, 2);
  static final BlockPos FAR = new BlockPos(GAP_X, FLOOR, 6);

  record Case(String name, float height, int gap, boolean passes) {}

  static final List<Case> CASES =
      List.of(
          new Case("tallestbuildstopsatatwoblockgap", 2.0F, 2, false),
          new Case("tallestbuildpassesathreeblockgap", 2.0F, 3, true),
          new Case("averagebuildpassesatwoblockgap", 1.95F, 2, true),
          new Case("lordpassesathreeblockgap", 2.3F, 3, true));

  @CustomTestProvider
  public List<TestFunction> gaps() {
    return CASES.stream()
        .map(
            gap ->
                new TestFunction(
                    "defaultBatch",
                    "routeheadroomgametest." + gap.name(),
                    ARENA,
                    40,
                    0L,
                    true,
                    context -> walkUnder(context, gap)))
        .toList();
  }

  private static void walkUnder(final TestContext context, final Case gap) {
    for (int x = 0; x < WIDTH; x++) {
      for (int z = 0; z < WIDTH; z++) {
        for (int y = ROOF; y <= WALL_TOP; y++) {
          context.setBlockState(x, y, z, Blocks.AIR);
        }
      }
    }
    for (int x = 0; x < WIDTH; x++) {
      for (int y = FLOOR; y <= WALL_TOP; y++) {
        final boolean open = x == GAP_X && y < FLOOR + gap.gap();
        context.setBlockState(x, y, WALL_Z, open ? Blocks.AIR : Blocks.STONE_BRICKS);
      }
    }
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, NEAR);
    vampire.setAiDisabled(true);
    vampire.setAppearance(vampire.lookId(), gap.height(), 1.0F);
    context.runAtTick(
        START,
        () -> {
          context.assertTrue(
              Math.abs(vampire.getHeight() - gap.height()) < 1.0E-3,
              "the vampire stands " + gap.height() + " tall, got " + vampire.getHeight());
          final Path path = vampire.getNavigation().findPathTo(context.getAbsolutePos(FAR), 0);
          final boolean through = path != null && path.reachesTarget();
          context.assertTrue(
              through == gap.passes(),
              "a "
                  + gap.height()
                  + " block vampire under a "
                  + gap.gap()
                  + " block gap: expected a way through "
                  + gap.passes()
                  + ", got "
                  + through);
          context.complete();
        });
  }
}
