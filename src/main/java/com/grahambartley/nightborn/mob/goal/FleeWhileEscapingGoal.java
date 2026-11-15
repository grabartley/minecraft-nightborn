package com.grahambartley.nightborn.mob.goal;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.mob.DownedMobRules;
import com.grahambartley.nightborn.vampire.Vampires;
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
    if (!DownedMobRules.flees(Downed.state(mob))) {
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
    return DownedMobRules.flees(Downed.state(mob)) && !mob.getNavigation().isIdle();
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
    final LivingEntity attacker = mob.getAttacker();
    if (attacker != null
        && attacker.isAlive()
        && mob.squaredDistanceTo(attacker) < THREAT_RANGE * THREAT_RANGE) {
      return attacker;
    }
    return mob.getWorld()
        .getClosestPlayer(
            mob.getX(),
            mob.getY(),
            mob.getZ(),
            THREAT_RANGE,
            entity ->
                entity instanceof PlayerEntity player
                    && !player.isSpectator()
                    && !player.isCreative()
                    && !Vampires.isVampire(player));
  }
}
