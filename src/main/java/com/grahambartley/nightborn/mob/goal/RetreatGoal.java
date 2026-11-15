package com.grahambartley.nightborn.mob.goal;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.mob.combat.Retreat;
import java.util.EnumSet;
import java.util.function.IntSupplier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class RetreatGoal extends Goal {

  static final int RETREAT_DISTANCE = 14;
  static final int RETREAT_HEIGHT = 6;

  private final PathAwareEntity mob;
  private final double speed;
  private final IntSupplier retreatHealth;
  @Nullable private LivingEntity threat;
  private int ticks;
  private long restUntil;

  public RetreatGoal(
      final PathAwareEntity mob, final double speed, final IntSupplier retreatHealth) {
    this.mob = mob;
    this.speed = speed;
    this.retreatHealth = retreatHealth;
    setControls(EnumSet.of(Control.MOVE));
  }

  @Override
  public boolean canStart() {
    final LivingEntity target = mob.getTarget();
    if (target == null
        || Downed.state(mob) != null
        || !Retreat.starts(
            health(), retreatHealth.getAsInt(), mob.getWorld().getTime() < restUntil)) {
      return false;
    }
    threat = target;
    return move();
  }

  @Override
  public boolean shouldContinue() {
    return threat != null
        && Downed.state(mob) == null
        && !Retreat.ends(health(), retreatHealth.getAsInt(), ticks);
  }

  @Override
  public void start() {
    ticks = 0;
  }

  @Override
  public void tick() {
    ticks++;
    if (mob.getNavigation().isIdle()) {
      move();
    }
  }

  @Override
  public void stop() {
    threat = null;
    restUntil = mob.getWorld().getTime() + Retreat.REST_TICKS;
    mob.getNavigation().stop();
  }

  private boolean move() {
    if (threat == null) {
      return false;
    }
    final Vec3d away =
        NoPenaltyTargeting.findFrom(mob, RETREAT_DISTANCE, RETREAT_HEIGHT, threat.getPos());
    if (away == null) {
      return false;
    }
    mob.getNavigation().startMovingTo(away.x, away.y, away.z, speed);
    return true;
  }

  private int health() {
    return Retreat.healthPercent(mob.getHealth(), mob.getMaxHealth());
  }
}
