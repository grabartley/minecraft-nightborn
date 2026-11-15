package com.grahambartley.nightborn.familiar.goal;

import com.grahambartley.nightborn.familiar.HumanFamiliarEntity;
import com.grahambartley.nightborn.familiar.Slights;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.player.PlayerEntity;

public class RetaliateGoal extends RevengeGoal {

  private final HumanFamiliarEntity familiar;

  public RetaliateGoal(final HumanFamiliarEntity familiar) {
    super(familiar);
    this.familiar = familiar;
  }

  @Override
  public boolean canStart() {
    return super.canStart()
        && Slights.retaliates(
            familiar.getAttacker() instanceof PlayerEntity player && familiar.isOwnedBy(player));
  }
}
