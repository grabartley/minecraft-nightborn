package com.grahambartley.nightborn.mob.goal;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.mob.DownedMobRules;
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

  public static boolean exposed(final PathAwareEntity mob) {
    final World world = mob.getWorld();
    return ShadeRules.seeksShade(
        world.getDimension().hasFixedTime(),
        world.getTimeOfDay(),
        world.isSkyVisible(BlockPos.ofFloored(mob.getX(), mob.getEyeY(), mob.getZ())),
        mob.isWet());
  }

  @Override
  public boolean canStart() {
    return !DownedMobRules.helpless(Downed.state(mob)) && exposed(mob) && targetShadedPos();
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
      final BlockPos pos =
          origin.add(
              random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS,
              random.nextInt(SEARCH_HEIGHT * 2 + 1) - SEARCH_HEIGHT,
              random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS);
      if (standsInShade(world, pos)) {
        return Vec3d.ofBottomCenter(pos);
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
