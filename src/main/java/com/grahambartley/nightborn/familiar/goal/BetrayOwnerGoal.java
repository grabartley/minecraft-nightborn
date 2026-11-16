package com.grahambartley.nightborn.familiar.goal;

import com.grahambartley.nightborn.familiar.HumanFamiliarEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.player.PlayerEntity;

public class BetrayOwnerGoal extends TrackTargetGoal {

  static final double REACH = 24.0;

  private final HumanFamiliarEntity familiar;

  public BetrayOwnerGoal(final HumanFamiliarEntity familiar) {
    super(familiar, false);
    this.familiar = familiar;
  }

  @Override
  public boolean canStart() {
    return sleepingOwner() != null;
  }

  @Override
  public void start() {
    familiar.setTarget(sleepingOwner());
    super.start();
  }

  private PlayerEntity sleepingOwner() {
    if (!familiar.turned() || familiar.owner().isEmpty()) {
      return null;
    }
    final PlayerEntity owner = familiar.getWorld().getPlayerByUuid(familiar.owner().get());
    if (owner == null
        || !owner.isSleeping()
        || familiar.squaredDistanceTo(owner) > REACH * REACH
        || !canTrack(owner, TargetPredicate.DEFAULT.ignoreVisibility())) {
      return null;
    }
    return owner;
  }
}
