package com.grahambartley.nightborn.servant.job.goal;

import com.grahambartley.nightborn.familiar.FamiliarClock;
import com.grahambartley.nightborn.servant.job.DayJob;
import com.grahambartley.nightborn.servant.job.DayWorker;
import com.grahambartley.nightborn.servant.job.JobOrder;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;

public class StandWatchGoal<T extends PathAwareEntity & DayWorker> extends Goal {

  static final double SPEED = 0.8;
  static final int POST_REACH = 3;

  private final T worker;

  public StandWatchGoal(final T worker) {
    this.worker = worker;
    setControls(EnumSet.of(Control.MOVE));
  }

  private Optional<BlockPos> post() {
    return worker.job().filter(order -> order.is(DayJob.STAND_WATCH)).map(JobOrder::place);
  }

  @Override
  public boolean canStart() {
    return worker.getTarget() == null
        && worker.willWork()
        && FamiliarClock.isWorkingHours(worker.getWorld())
        && post()
            .filter(post -> !worker.getBlockPos().isWithinDistance(post, POST_REACH))
            .isPresent();
  }

  @Override
  public boolean shouldContinue() {
    return canStart() && !worker.getNavigation().isIdle();
  }

  @Override
  public void start() {
    post()
        .ifPresent(
            post ->
                worker
                    .getNavigation()
                    .startMovingTo(post.getX() + 0.5, post.getY(), post.getZ() + 0.5, SPEED));
  }

  @Override
  public void stop() {
    worker.getNavigation().stop();
  }
}
