package com.grahambartley.nightborn.levitation;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;

public final class FloatRoom {

  private FloatRoom() {}

  public static boolean ahead(final MobEntity mob) {
    final EntityDimensions body = mob.getDimensions(EntityPose.STANDING);
    final double ground =
        FloatRoomRules.groundFeet(
            mob.getY(), LevitationGround.surfaceBelow(mob), Levitation.isLevitating(mob));
    if (!fits(mob, body, mob.getX(), ground, mob.getZ())) {
      return false;
    }
    final Path path = mob.getNavigation().getCurrentPath();
    if (path == null || path.isFinished()) {
      return true;
    }
    final int last = FloatRoomRules.lastNodeChecked(path.getCurrentNodeIndex(), path.getLength());
    for (int index = Math.max(0, path.getCurrentNodeIndex()); index < last; index++) {
      final BlockPos node = path.getNodePos(index);
      if (!fits(
          mob,
          body,
          node.getX() + 0.5,
          LandPathNodeMaker.getFeetY(mob.getWorld(), node),
          node.getZ() + 0.5)) {
        return false;
      }
    }
    return true;
  }

  private static boolean fits(
      final MobEntity mob,
      final EntityDimensions body,
      final double x,
      final double ground,
      final double z) {
    return mob.getWorld()
        .isSpaceEmpty(mob, FloatRoomRules.floatingBody(body.width(), body.height(), x, ground, z));
  }
}
