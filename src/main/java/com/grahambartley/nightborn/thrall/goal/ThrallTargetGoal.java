package com.grahambartley.nightborn.thrall.goal;

import com.grahambartley.nightborn.servant.ServantTargets;
import com.grahambartley.nightborn.thrall.FollowRules;
import com.grahambartley.nightborn.thrall.ThrallEntity;
import java.util.Comparator;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.Monster;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class ThrallTargetGoal extends TrackTargetGoal {

  static final int RETHINK_TICKS = 10;

  private final ThrallEntity thrall;
  @Nullable private LivingEntity chosen;
  private int rethink;

  public ThrallTargetGoal(final ThrallEntity thrall) {
    super(thrall, false);
    this.thrall = thrall;
    setControls(EnumSet.of(Control.TARGET));
  }

  @Override
  public boolean canStart() {
    if (--rethink > 0) {
      return false;
    }
    rethink = RETHINK_TICKS;
    chosen = pick();
    return chosen != null;
  }

  @Override
  public void start() {
    thrall.setTarget(chosen);
    super.start();
  }

  @Nullable
  private LivingEntity pick() {
    final LivingEntity foe = ServantTargets.mastersFoe(thrall, this::allowed);
    if (foe != null) {
      return foe;
    }
    final Vec3d post = Vec3d.ofBottomCenter(thrall.post());
    return thrall
        .getWorld()
        .getEntitiesByClass(
            LivingEntity.class,
            new Box(post, post).expand(16.0),
            living ->
                living instanceof Monster
                    && FollowRules.guards(thrall.mode(), Math.sqrt(living.squaredDistanceTo(post)))
                    && allowed(living))
        .stream()
        .min(Comparator.comparingDouble(thrall::squaredDistanceTo))
        .orElse(null);
  }

  private boolean allowed(@Nullable final LivingEntity candidate) {
    return candidate != null
        && ServantTargets.mayAttack(thrall, candidate)
        && canTrack(candidate, TargetPredicate.DEFAULT);
  }
}
