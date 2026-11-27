package com.grahambartley.nightborn.wild;

import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.mob.ShadeRules;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.brain.Intent;
import com.grahambartley.nightborn.mob.brain.IntentGoal;
import java.util.EnumSet;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class WildTravelGoal extends Goal implements IntentGoal {

  static final int HEIGHT = 7;
  static final int DAY_TRIES = 4;
  static final float TURN = MathHelper.HALF_PI;

  private final VampireEntity vampire;
  private float heading = Float.NaN;
  private int restUntil;

  public WildTravelGoal(final VampireEntity vampire) {
    this.vampire = vampire;
    setControls(EnumSet.of(Control.MOVE));
  }

  @Override
  public boolean canStart() {
    if (!travels() || vampire.age < restUntil || !vampire.getNavigation().isIdle()) {
      return false;
    }
    if (night() ? headOut() : hop()) {
      return true;
    }
    rest();
    return false;
  }

  @Override
  public boolean shouldContinue() {
    return travels() && !vampire.getNavigation().isIdle();
  }

  @Override
  public void stop() {
    vampire.getNavigation().stop();
    if (vampire.getTarget() == null) {
      rest();
    }
  }

  private void rest() {
    restUntil = vampire.age + WildPace.pause(night(), vampire.getRandom().nextInt());
  }

  private boolean travels() {
    return WildPace.travels(
        WandererAttachment.get(vampire).filter(wanderer -> !wanderer.watcher()).isPresent(),
        vampire.getTarget() != null,
        vampire.isSleeping());
  }

  private boolean night() {
    return NightTime.isNight(
        vampire.getWorld().getDimension().hasFixedTime(), vampire.getWorld().getTimeOfDay());
  }

  private boolean headOut() {
    if (Float.isNaN(heading)) {
      heading = vampire.getRandom().nextFloat() * MathHelper.TAU;
    }
    final int leg = WildPace.NIGHT_LEG;
    final Vec3d ahead =
        vampire.getPos().add(MathHelper.cos(heading) * leg, 0.0, MathHelper.sin(heading) * leg);
    final Vec3d step = FuzzyTargeting.findTo(vampire, leg / 2, HEIGHT, ahead);
    if (step == null) {
      heading += (vampire.getRandom().nextBoolean() ? TURN : -TURN);
      return false;
    }
    return vampire.getNavigation().startMovingTo(step.x, step.y, step.z, 1.0);
  }

  private boolean hop() {
    for (int i = 0; i < DAY_TRIES; i++) {
      final Vec3d spot = FuzzyTargeting.find(vampire, WildPace.DAY_LEG, HEIGHT);
      final Path path =
          spot == null ? null : vampire.getNavigation().findPathTo(BlockPos.ofFloored(spot), 0);
      if (path != null
          && path.reachesTarget()
          && shaded(path)
          && vampire.getNavigation().startMovingAlong(path, 1.0)) {
        return true;
      }
    }
    return false;
  }

  private boolean shaded(final Path path) {
    for (int i = 0; i < path.getLength(); i++) {
      if (!ShadeRules.shadedAt(vampire.getWorld(), path.getNodePos(i))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Intent intent() {
    return Intent.TRAVEL;
  }
}
