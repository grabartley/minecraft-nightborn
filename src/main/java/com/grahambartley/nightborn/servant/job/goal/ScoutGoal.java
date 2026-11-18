package com.grahambartley.nightborn.servant.job.goal;

import com.grahambartley.nightborn.familiar.FamiliarClock;
import com.grahambartley.nightborn.servant.job.DayJob;
import com.grahambartley.nightborn.servant.job.DayWorker;
import com.grahambartley.nightborn.servant.job.JobOrder;
import com.grahambartley.nightborn.servant.job.ScoutChoices;
import com.grahambartley.nightborn.servant.job.ScoutProgress;
import com.grahambartley.nightborn.servant.job.ScoutReports;
import com.grahambartley.nightborn.servant.job.ScoutSightings;
import com.grahambartley.nightborn.servant.job.ScoutTicket;
import com.grahambartley.nightborn.servant.job.ScoutTrip;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ScoutGoal<T extends PathAwareEntity & DayWorker> extends Goal {

  static final double SPEED = 0.7;
  static final int HOME_REACH = 3;
  static final int STEP = 16;
  static final int STEP_HEIGHT = 7;
  static final int GIVE_UP_TICKS = 1200;

  private final T worker;
  private ScoutProgress progress = ScoutProgress.START;
  private int ticks;

  public ScoutGoal(final T worker) {
    this.worker = worker;
    setControls(EnumSet.of(Control.MOVE));
  }

  private Optional<ScoutTrip> trip() {
    return worker.job().filter(order -> order.is(DayJob.SCOUT)).flatMap(JobOrder::trip);
  }

  @Override
  public boolean canStart() {
    return worker.getWorld() instanceof ServerWorld
        && worker.willWork()
        && trip()
            .filter(trip -> trip.returning() || FamiliarClock.isWorkingHours(worker.getWorld()))
            .isPresent();
  }

  @Override
  public boolean shouldContinue() {
    return canStart();
  }

  @Override
  public void start() {
    progress = ScoutProgress.START;
    ticks = 0;
  }

  @Override
  public void stop() {
    worker.getNavigation().stop();
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void tick() {
    if (!(worker.getWorld() instanceof ServerWorld world) || worker.job().isEmpty()) {
      return;
    }
    final JobOrder order = worker.job().get();
    final Optional<ScoutTrip> current = order.trip();
    if (current.isEmpty()) {
      return;
    }
    final ScoutTrip trip = current.get();
    final boolean check = ticks++ % ScoutTicket.REFRESH_TICKS == 0;
    final BlockPos goal = trip.returning() ? order.place() : trip.target();
    final double distanceSquared = ScoutChoices.flatDistanceSquared(worker.getBlockPos(), goal);
    final int sight = ServerConfigService.current().servants().scoutSight();
    if (ScoutProgress.arrived(distanceSquared, trip.returning() ? HOME_REACH : sight)) {
      arrive(world, order, trip, sight);
      return;
    }
    if (check) {
      ScoutTicket.keep(world, worker);
    }
    if (check) {
      progress = progress.step(Math.sqrt(distanceSquared), ScoutTicket.REFRESH_TICKS);
      if (progress.lost(GIVE_UP_TICKS)) {
        giveUp(world, order, trip);
        return;
      }
    }
    if (check || worker.getNavigation().isIdle()) {
      walkToward(goal);
    }
  }

  private void arrive(
      final ServerWorld world, final JobOrder order, final ScoutTrip trip, final int sight) {
    worker.getNavigation().stop();
    if (!trip.returning()) {
      worker.assign(order.withTrip(trip.reported(ScoutSightings.around(world, worker, sight))));
      progress = ScoutProgress.START;
      return;
    }
    if (ScoutReports.tell(world, worker, trip)) {
      worker.dismiss();
    }
  }

  private void giveUp(final ServerWorld world, final JobOrder order, final ScoutTrip trip) {
    progress = ScoutProgress.START;
    if (!trip.returning()) {
      worker.assign(order.withTrip(trip.lostTheWay()));
      return;
    }
    if (ScoutReports.tell(world, worker, trip)) {
      worker.dismiss();
    }
  }

  private void walkToward(final BlockPos goal) {
    final Vec3d toward = Vec3d.ofBottomCenter(goal);
    Vec3d step = NoPenaltyTargeting.findTo(worker, STEP, STEP_HEIGHT, toward, Math.PI / 2);
    if (step == null) {
      step = FuzzyTargeting.findTo(worker, STEP, STEP_HEIGHT, toward);
    }
    if (step != null) {
      worker.getNavigation().startMovingTo(step.x, step.y, step.z, SPEED);
    }
  }
}
