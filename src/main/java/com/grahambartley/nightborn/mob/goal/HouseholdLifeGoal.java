package com.grahambartley.nightborn.mob.goal;

import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.household.HouseholdPlanner;
import com.grahambartley.nightborn.mob.household.HouseholdTask;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public class HouseholdLifeGoal extends Goal {

  private final VampireEntity vampire;
  @Nullable private HouseholdTask task;

  public HouseholdLifeGoal(final VampireEntity vampire) {
    this.vampire = vampire;
    setControls(EnumSet.of(Control.MOVE, Control.LOOK));
  }

  @Override
  public boolean canStart() {
    return HouseholdPlanner.calm(vampire);
  }

  @Override
  public boolean shouldContinue() {
    return HouseholdPlanner.calm(vampire);
  }

  @Override
  public void start() {
    task = null;
  }

  @Override
  public void tick() {
    if (!(vampire.getWorld() instanceof ServerWorld world)) {
      return;
    }
    final long now = world.getTime();
    final Optional<VampireEntity> speaker = engagedBy(world, now);
    if (speaker.isPresent()) {
      vampire.getNavigation().stop();
      vampire.getLookControl().lookAt(speaker.get());
      return;
    }
    if (task == null || !task.tick()) {
      endTask();
      task = HouseholdPlanner.next(vampire, world, now);
    }
  }

  @Override
  public void stop() {
    endTask();
  }

  private void endTask() {
    if (task != null) {
      task.stop();
      task = null;
    }
  }

  private Optional<VampireEntity> engagedBy(final ServerWorld world, final long now) {
    return vampire
        .householdLife()
        .engagement(now)
        .map(engagement -> world.getEntityById(engagement.partnerId()))
        .filter(VampireEntity.class::isInstance)
        .map(VampireEntity.class::cast);
  }
}
