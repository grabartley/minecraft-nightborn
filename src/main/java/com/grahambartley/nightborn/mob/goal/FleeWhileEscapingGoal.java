package com.grahambartley.nightborn.mob.goal;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedRules;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.mob.Prey;
import com.grahambartley.nightborn.mob.ShadeRules;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class FleeWhileEscapingGoal extends Goal {

  static final double THREAT_RANGE = 16.0;
  static final int FLEE_DISTANCE = 16;
  static final int FLEE_HEIGHT = 7;

  private final PathAwareEntity mob;
  private final double speed;
  @Nullable private Vec3d destination;

  public FleeWhileEscapingGoal(final PathAwareEntity mob, final double speed) {
    this.mob = mob;
    this.speed = speed;
    setControls(EnumSet.of(Control.MOVE));
  }

  @Override
  public boolean canStart() {
    if (!DownedRules.flees(Downed.state(mob)) || ShadeRules.endangered(mob)) {
      return false;
    }
    final LivingEntity threat = threat();
    if (threat == null) {
      return false;
    }
    destination = NoPenaltyTargeting.findFrom(mob, FLEE_DISTANCE, FLEE_HEIGHT, threat.getPos());
    return destination != null;
  }

  @Override
  public boolean shouldContinue() {
    return DownedRules.flees(Downed.state(mob)) && !mob.getNavigation().isIdle();
  }

  @Override
  public void start() {
    mob.setTarget(null);
    if (destination != null) {
      mob.getNavigation().startMovingTo(destination.x, destination.y, destination.z, speed);
    }
  }

  @Override
  public void stop() {
    destination = null;
  }

  @Nullable
  private LivingEntity threat() {
    final DownedState state = Downed.state(mob);
    final LivingEntity hunter =
        state == null
            ? null
            : state.downedBy().map(id -> mob.getWorld().getPlayerByUuid(id)).orElse(null);
    if (near(hunter)) {
      return hunter;
    }
    final LivingEntity attacker = mob.getAttacker();
    if (near(attacker)) {
      return attacker;
    }
    return mob.getWorld()
        .getClosestPlayer(
            mob.getX(),
            mob.getY(),
            mob.getZ(),
            THREAT_RANGE,
            entity -> entity instanceof PlayerEntity player && Prey.isPrey(player));
  }

  private boolean near(@Nullable final LivingEntity threat) {
    return threat != null
        && threat.isAlive()
        && mob.squaredDistanceTo(threat) < THREAT_RANGE * THREAT_RANGE;
  }
}
