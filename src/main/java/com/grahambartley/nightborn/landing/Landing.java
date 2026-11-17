package com.grahambartley.nightborn.landing;

import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public final class Landing {

  static final int FLOOR_SEARCH = 3;

  private Landing() {}

  public static Optional<Vec3d> reach(final LivingEntity caster, final Vec3d wanted) {
    final Vec3d from = caster.getEyePos();
    final HitResult hit =
        caster
            .getWorld()
            .raycast(
                new RaycastContext(
                    from,
                    wanted,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    caster));
    final Vec3d reach = hit.getType() == HitResult.Type.MISS ? wanted : hit.getPos();
    for (final Vec3d point : LandingPath.candidates(from, reach)) {
      final Optional<Vec3d> feet = standing(caster, point);
      if (feet.isPresent()) {
        return feet;
      }
    }
    return Optional.empty();
  }

  private static Optional<Vec3d> standing(final LivingEntity caster, final Vec3d eye) {
    final World world = caster.getWorld();
    final double feetY = eye.y - caster.getStandingEyeHeight();
    final BlockPos start = BlockPos.ofFloored(eye.x, feetY, eye.z);
    for (int down = 0; down <= FLOOR_SEARCH; down++) {
      final BlockPos floor = start.down(down);
      final VoxelShape shape = world.getBlockState(floor).getCollisionShape(world, floor);
      if (shape.isEmpty()) {
        continue;
      }
      final Vec3d at = new Vec3d(eye.x, floor.getY() + shape.getMax(Direction.Axis.Y), eye.z);
      final Box body = caster.getDimensions(caster.getPose()).getBoxAt(at);
      if (world.isSpaceEmpty(caster, body)
          && !world.containsFluid(body)
          && !LandingHazards.burns(world.getBlockState(floor), world.getStatesInBox(body))) {
        return Optional.of(at);
      }
      return Optional.empty();
    }
    return Optional.empty();
  }

  public static void move(final LivingEntity caster, final Vec3d to) {
    if (!(caster.getWorld() instanceof ServerWorld world)) {
      return;
    }
    caster.teleport(
        world,
        to.x,
        to.y,
        to.z,
        EnumSet.noneOf(PositionFlag.class),
        caster.getYaw(),
        caster.getPitch());
    caster.fallDistance = 0.0F;
  }
}
