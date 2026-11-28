package com.grahambartley.nightborn.mob.goal;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.mob.BatSteering;
import com.grahambartley.nightborn.mob.brain.FormHold;
import com.grahambartley.nightborn.mob.brain.Intent;
import com.grahambartley.nightborn.mob.brain.IntentGoal;
import com.grahambartley.nightborn.mob.combat.MobFighter;
import com.grahambartley.nightborn.skill.BatFormPower;
import com.grahambartley.nightborn.skill.Powers;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BatFlightGoal extends Goal implements IntentGoal {

  static final Vec3d SINK = new Vec3d(0.0, -0.08, 0.0);
  static final double HOVER_DRAG = 0.5;
  static final int GROUND_LOOKED = 2;

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
    final Optional<BatFormPower> form = Powers.ownedBatForm(mob);
    final boolean chasing =
        target != null && BatFormPower.chases(form, target.isAlive(), mob.distanceTo(target));
    final FormHold hold = hold();
    final Vec3d course = hold == null ? null : hold.course().orElse(null);
    final BatCourse next =
        BatCourse.decide(
            chasing,
            target != null && target.isAlive(),
            course == null ? Double.NaN : mob.getPos().distanceTo(course),
            form.map(BatFormPower::landRange).orElse(0.0F),
            hold != null && hold.holds(mob.getWorld().getTime()),
            groundBelow());
    switch (next) {
      case CHASE -> steer(target.getEyePos());
      case FLY_ON -> steer(course);
      case HOVER -> hover();
      case LAND -> land(hold);
    }
  }

  private void land(@Nullable final FormHold hold) {
    if (BatForm.leave(mob)) {
      if (hold != null) {
        hold.arrive();
      }
    } else {
      hover();
    }
  }

  private void hover() {
    mob.setVelocity(mob.getVelocity().multiply(HOVER_DRAG).add(SINK));
  }

  private void steer(final Vec3d toward) {
    final Vec3d velocity = BatSteering.toward(mob.getVelocity(), toward.subtract(mob.getPos()));
    mob.setVelocity(velocity);
    final float yaw = BatSteering.yaw(velocity);
    mob.setYaw(yaw);
    mob.setBodyYaw(yaw);
    mob.setHeadYaw(yaw);
  }

  private boolean groundBelow() {
    final World world = mob.getWorld();
    final BlockPos feet = mob.getBlockPos();
    for (int step = 0; step <= GROUND_LOOKED; step++) {
      final BlockPos at = feet.down(step);
      if (!world.getBlockState(at).getCollisionShape(world, at).isEmpty()) {
        return true;
      }
    }
    return false;
  }

  @Nullable
  private FormHold hold() {
    return mob instanceof MobFighter fighter ? fighter.mobFighting().hold() : null;
  }

  @Override
  public Intent intent() {
    return mob.getTarget() == null ? Intent.TRAVEL : Intent.CLOSE_IN;
  }
}
