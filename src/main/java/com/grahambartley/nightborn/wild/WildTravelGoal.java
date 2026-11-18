package com.grahambartley.nightborn.wild;

import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.mob.VampireEntity;
import java.util.EnumSet;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class WildTravelGoal extends Goal {

  static final int LEG = 48;
  static final int REACH = 24;
  static final int HEIGHT = 7;
  static final float TURN = MathHelper.HALF_PI;

  private final VampireEntity vampire;
  private float heading = Float.NaN;

  public WildTravelGoal(final VampireEntity vampire) {
    this.vampire = vampire;
    setControls(EnumSet.of(Control.MOVE));
  }

  @Override
  public boolean canStart() {
    return travels() && vampire.getNavigation().isIdle() && headOut();
  }

  @Override
  public boolean shouldContinue() {
    return travels() && !vampire.getNavigation().isIdle();
  }

  @Override
  public void stop() {
    vampire.getNavigation().stop();
  }

  private boolean travels() {
    return WandererAttachment.get(vampire).filter(wanderer -> !wanderer.watcher()).isPresent()
        && vampire.getTarget() == null
        && !vampire.isSleeping()
        && NightTime.isNight(
            vampire.getWorld().getDimension().hasFixedTime(), vampire.getWorld().getTimeOfDay());
  }

  private boolean headOut() {
    if (Float.isNaN(heading)) {
      heading = vampire.getRandom().nextFloat() * MathHelper.TAU;
    }
    final Vec3d ahead =
        vampire.getPos().add(MathHelper.cos(heading) * LEG, 0.0, MathHelper.sin(heading) * LEG);
    final Vec3d step = FuzzyTargeting.findTo(vampire, REACH, HEIGHT, ahead);
    if (step == null) {
      heading += (vampire.getRandom().nextBoolean() ? TURN : -TURN);
      return false;
    }
    return vampire.getNavigation().startMovingTo(step.x, step.y, step.z, 1.0);
  }
}
