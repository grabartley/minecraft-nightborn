package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.mob.stage.StageLook;
import software.bernie.geckolib.animation.RawAnimation;

public final class StageClips {

  private StageClips() {}

  public static RawAnimation clip(final VampirePose pose, final StageLook look) {
    return pose == VampirePose.IDLE
        ? look.idle().map(ClipCache::loop).orElse(pose.clip())
        : pose.clip();
  }
}
