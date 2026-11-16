package com.grahambartley.nightborn.mob.goal;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.mob.BatSteering;
import com.grahambartley.nightborn.skill.BatFormPower;
import com.grahambartley.nightborn.skill.Powers;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;

public class BatFlightGoal extends Goal {

  static final Vec3d SINK = new Vec3d(0.0, -0.08, 0.0);
  static final double HOVER_DRAG = 0.5;

  private final MobEntity mob;

  public BatFlightGoal(final MobEntity mob) {
    this.mob = mob;
    setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
  }

  @Override
  public boolean canStart() {
    return BatForm.isBat(mob);
  }

  @Override
  public boolean shouldContinue() {
    return BatForm.isBat(mob);
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void start() {
    mob.getNavigation().stop();
  }

  @Override
  public void tick() {
    final LivingEntity target = mob.getTarget();
    final boolean chasing =
        target != null
            && BatFormPower.chases(
                Powers.ownedBatForm(mob), target.isAlive(), mob.distanceTo(target));
    if (!chasing) {
      if (!BatForm.leave(mob)) {
        mob.setVelocity(mob.getVelocity().multiply(HOVER_DRAG).add(SINK));
      }
      return;
    }
    final Vec3d velocity =
        BatSteering.toward(mob.getVelocity(), target.getEyePos().subtract(mob.getPos()));
    mob.setVelocity(velocity);
    final float yaw = BatSteering.yaw(velocity);
    mob.setYaw(yaw);
    mob.setBodyYaw(yaw);
    mob.setHeadYaw(yaw);
  }
}
