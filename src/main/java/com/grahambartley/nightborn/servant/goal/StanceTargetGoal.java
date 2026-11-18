package com.grahambartley.nightborn.servant.goal;

import com.grahambartley.nightborn.servant.FollowRules;
import com.grahambartley.nightborn.servant.ServantTargets;
import com.grahambartley.nightborn.servant.Stanced;
import java.util.Comparator;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class StanceTargetGoal extends TrackTargetGoal {

  static final int RETHINK_TICKS = 10;
  static final double LOOK_RANGE = 16.0;

  private final PathAwareEntity keeper;
  private final Stanced stanced;
  @Nullable private LivingEntity chosen;
  private int rethink;

  public StanceTargetGoal(final PathAwareEntity keeper, final Stanced stanced) {
    super(keeper, false);
    this.keeper = keeper;
    this.stanced = stanced;
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
    keeper.setTarget(chosen);
    super.start();
  }

  @Nullable
  private LivingEntity pick() {
    if (stanced.stance().isEmpty()) {
      return null;
    }
    final LivingEntity foe = ServantTargets.mastersFoe(keeper, this::allowed);
    if (foe != null) {
      return foe;
    }
    final Vec3d post = Vec3d.ofBottomCenter(stanced.post());
    return keeper
        .getWorld()
        .getEntitiesByClass(
            LivingEntity.class,
            new Box(post, post).expand(LOOK_RANGE),
            living ->
                stanced.guardsAgainst(living)
                    && stanced
                        .stance()
                        .filter(
                            stance ->
                                FollowRules.guards(
                                    stance, Math.sqrt(living.squaredDistanceTo(post))))
                        .isPresent()
                    && allowed(living))
        .stream()
        .min(Comparator.comparingDouble(keeper::squaredDistanceTo))
        .orElse(null);
  }

  private boolean allowed(@Nullable final LivingEntity candidate) {
    return candidate != null
        && ServantTargets.mayAttack(keeper, candidate)
        && canTrack(candidate, TargetPredicate.DEFAULT);
  }
}
