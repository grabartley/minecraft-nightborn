package com.grahambartley.nightborn.mob.goal;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedRules;
import com.grahambartley.nightborn.mob.ShadeRules;
import net.minecraft.entity.ai.goal.EscapeSunlightGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SeekShadeGoal extends EscapeSunlightGoal {

  static final int SEARCH_TRIES = 24;
  static final int SEARCH_RADIUS = 12;
  static final int SEARCH_HEIGHT = 4;

  public SeekShadeGoal(final PathAwareEntity mob, final double speed) {
    super(mob, speed);
  }

  @Override
  public boolean canStart() {
    return !DownedRules.helpless(Downed.state(mob))
        && ShadeRules.endangered(mob)
        && targetShadedPos();
  }

  @Override
  public void start() {
    mob.setTarget(null);
    super.start();
  }

  @Nullable
  @Override
  public Vec3d locateShadedPos() {
    final World world = mob.getWorld();
    final Random random = mob.getRandom();
    final BlockPos origin = mob.getBlockPos();
    for (int i = 0; i < SEARCH_TRIES; i++) {
      final int dx = random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
      final int dz = random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
      for (int dy = SEARCH_HEIGHT; dy >= -SEARCH_HEIGHT; dy--) {
        final BlockPos pos = origin.add(dx, dy, dz);
        if (standsInShade(world, pos)) {
          return Vec3d.ofBottomCenter(pos);
        }
      }
    }
    return null;
  }

  private static boolean standsInShade(final World world, final BlockPos feet) {
    return world.isAir(feet)
        && world.isAir(feet.up())
        && world.getBlockState(feet.down()).isSolidBlock(world, feet.down())
        && !world.isSkyVisible(feet.up());
  }
}
