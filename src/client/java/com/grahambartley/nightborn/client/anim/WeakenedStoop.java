package com.grahambartley.nightborn.client.anim;

import net.minecraft.entity.EntityPose;

public final class WeakenedStoop {

  private WeakenedStoop() {}

  public static boolean onFoot(
      final EntityPose pose, final boolean riding, final boolean flying, final boolean usingItem) {
    return pose == EntityPose.STANDING && !riding && !flying && !usingItem;
  }
}
