package com.grahambartley.nightborn.servant.job.goal;

import com.grahambartley.nightborn.familiar.ErrandTransfer;
import com.grahambartley.nightborn.familiar.FamiliarClock;
import com.grahambartley.nightborn.servant.ChestMarking;
import com.grahambartley.nightborn.servant.job.DayJob;
import com.grahambartley.nightborn.servant.job.DayWorker;
import com.grahambartley.nightborn.servant.job.JobChest;
import com.grahambartley.nightborn.servant.job.JobOrder;
import com.grahambartley.nightborn.servant.job.RoundRules;
import com.grahambartley.nightborn.servant.job.RoundRules.Leg;
import com.grahambartley.nightborn.servant.job.RoundRules.Step;
import java.util.EnumSet;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public abstract class ChestRoundGoal<T extends PathAwareEntity & DayWorker> extends Goal {

  static final double SPEED = 0.7;
  static final double REACH = 2.5;

  protected final T worker;
  private final DayJob job;
  @Nullable private Leg leg;
  @Nullable private BlockPos chest;
  private boolean nothingLeft;
  private boolean idle;
  private int ticksOnLeg;
  private long restUntil;

  protected ChestRoundGoal(final T worker, final DayJob job) {
    this.worker = worker;
    this.job = job;
    setControls(EnumSet.of(Control.MOVE, Control.LOOK));
  }

  protected abstract void stock(Inventory chest);

  protected abstract boolean readyToWork();

  protected abstract Step work(ServerWorld world);

  protected abstract void forgetTarget();

  @Override
  public boolean canStart() {
    if (!(worker.getWorld() instanceof ServerWorld world)
        || world.getTime() < restUntil
        || !working()) {
      return false;
    }
    chest = worker.owner().flatMap(owner -> JobChest.near(world, owner, place())).orElse(null);
    return chest != null;
  }

  private boolean working() {
    return worker.getTarget() == null
        && worker.willWork()
        && worker.doing(job)
        && FamiliarClock.isWorkingHours(worker.getWorld());
  }

  protected BlockPos place() {
    return worker.job().map(JobOrder::place).orElse(worker.getBlockPos());
  }

  @Override
  public void start() {
    nothingLeft = false;
    idle = false;
    enter(Leg.CHEST);
  }

  @Override
  public boolean shouldContinue() {
    return leg != null && leg != Leg.END && working();
  }

  @Override
  public void stop() {
    leg = null;
    forgetTarget();
    worker.getNavigation().stop();
    restUntil = worker.getWorld().getTime() + RoundRules.restTicks(idle);
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void tick() {
    if (leg == null || !(worker.getWorld() instanceof ServerWorld world)) {
      return;
    }
    ticksOnLeg++;
    if (leg == Leg.CHEST) {
      chestLeg(world);
      return;
    }
    if (leg == Leg.WORK) {
      final Step step = work(world);
      nothingLeft = step == Step.NOTHING_LEFT;
      final Leg next = RoundRules.afterStep(step);
      if (next != leg) {
        enter(next);
      }
    }
  }

  private void chestLeg(final ServerWorld world) {
    if (chest == null || RoundRules.stuck(ticksOnLeg)) {
      enter(Leg.END);
      return;
    }
    if (!walkTo(Vec3d.ofBottomCenter(chest))) {
      return;
    }
    if (!ChestMarking.isErrandChest(world, chest)) {
      worker
          .owner()
          .ifPresent(
              owner ->
                  ChestMarking.forget(
                      world.getServer(), owner, GlobalPos.create(world.getRegistryKey(), chest)));
      enter(Leg.END);
      return;
    }
    final Inventory into = HopperBlockEntity.getInventoryAt(world, chest);
    if (into == null) {
      enter(Leg.END);
      return;
    }
    ErrandTransfer.deliverAll(worker.getInventory(), into);
    final boolean deliveredAll = worker.getInventory().isEmpty();
    if (deliveredAll && !nothingLeft) {
      stock(into);
      if (!readyToWork()) {
        ErrandTransfer.deliverAll(worker.getInventory(), into);
      }
    }
    final Leg next = RoundRules.afterChest(deliveredAll, readyToWork(), nothingLeft);
    idle = next == Leg.END;
    enter(next);
  }

  private void enter(final Leg next) {
    leg = next;
    ticksOnLeg = 0;
  }

  protected int ticksOnLeg() {
    return ticksOnLeg;
  }

  protected void restartLegClock() {
    ticksOnLeg = 0;
  }

  protected boolean walkTo(final Vec3d spot) {
    if (worker.getPos().isInRange(spot, REACH)) {
      return true;
    }
    if (ticksOnLeg % 20 == 1 || worker.getNavigation().isIdle()) {
      worker.getNavigation().startMovingTo(spot.x, spot.y, spot.z, SPEED);
    }
    return false;
  }
}
