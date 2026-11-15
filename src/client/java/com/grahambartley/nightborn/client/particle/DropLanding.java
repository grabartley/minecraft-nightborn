package com.grahambartley.nightborn.client.particle;

import java.util.Optional;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class DropLanding {

  private DropLanding() {}

  public static Optional<Direction> face(
      final boolean onGround,
      final double beforeX,
      final double afterX,
      final double beforeZ,
      final double afterZ) {
    if (onGround) {
      return Optional.of(Direction.UP);
    }
    if (beforeX != 0.0 && afterX == 0.0) {
      return Optional.of(beforeX > 0.0 ? Direction.WEST : Direction.EAST);
    }
    if (beforeZ != 0.0 && afterZ == 0.0) {
      return Optional.of(beforeZ > 0.0 ? Direction.NORTH : Direction.SOUTH);
    }
    return Optional.empty();
  }

  public static Vec3d contact(final Vec3d centre, final Direction face, final double halfWidth) {
    if (face.getAxis() == Direction.Axis.Y) {
      return centre;
    }
    return centre.subtract(face.getOffsetX() * halfWidth, 0.0, face.getOffsetZ() * halfWidth);
  }
}
