package com.grahambartley.nightborn.servant.job.goal;

import com.grahambartley.nightborn.familiar.ErrandTransfer;
import com.grahambartley.nightborn.familiar.FamiliarClock;
import com.grahambartley.nightborn.servant.ChestMarking;
import com.grahambartley.nightborn.servant.job.DayJob;
import com.grahambartley.nightborn.servant.job.DayWorker;
import com.grahambartley.nightborn.servant.job.GatherTarget;
import com.grahambartley.nightborn.servant.job.Harvest;
import com.grahambartley.nightborn.servant.job.JobChest;
import com.grahambartley.nightborn.servant.job.JobOrder;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import org.jetbrains.annotations.Nullable;

public class GatherGoal<T extends PathAwareEntity & DayWorker> extends Goal {

  static final double SPEED = 0.7;
  static final double REACH = 2.5;
  static final int REST_TICKS = 100;
  static final int STUCK_TICKS = 200;

  enum Leg {
    HARVEST,
    DELIVER
  }

  private final T worker;
  private final Set<BlockPos> unreachable = new HashSet<>();
  @Nullable private Leg leg;
  @Nullable private BlockPos target;
  @Nullable private BlockPos chest;
  private int ticksOnTarget;
  private long restUntil;

  public GatherGoal(final T worker) {
    this.worker = worker;
    setControls(EnumSet.of(Control.MOVE, Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (!(worker.getWorld() instanceof ServerWorld world)
        || world.getTime() < restUntil
        || !working()) {
      return false;
    }
    final Optional<BlockPos> found =
        worker.owner().flatMap(owner -> JobChest.near(world, owner, place()));
    chest = found.orElse(null);
    return chest != null;
  }

  private boolean working() {
    return worker.getTarget() == null
        && worker.willWork()
        && worker.doing(DayJob.GATHER)
        && FamiliarClock.isWorkingHours(worker.getWorld());
  }

  private BlockPos place() {
    return worker.job().map(JobOrder::place).orElse(worker.getBlockPos());
  }

  @Override
  public void start() {
    leg = Leg.HARVEST;
    target = null;
    unreachable.clear();
  }

  @Override
  public boolean shouldContinue() {
    return leg != null && working();
  }

  @Override
  public void stop() {
    leg = null;
    target = null;
    worker.getNavigation().stop();
    restUntil = worker.getWorld().getTime() + REST_TICKS;
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
    if (leg == Leg.HARVEST) {
      harvestLeg(world);
    } else {
      deliverLeg(world);
    }
  }

  private void harvestLeg(final ServerWorld world) {
    if (target == null) {
      target = nextTarget(world);
      ticksOnTarget = 0;
      if (target == null) {
        leg = worker.getInventory().isEmpty() ? null : Leg.DELIVER;
        return;
      }
    }
    if (++ticksOnTarget > STUCK_TICKS) {
      unreachable.add(target);
      target = null;
      return;
    }
    if (!reached(target)) {
      return;
    }
    worker.getLookControl().lookAt(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
    if (Harvest.reap(world, target, worker, worker.getInventory())) {
      worker.swingHand(Hand.MAIN_HAND);
      target = null;
    } else if (Harvest.kind(world, target) == GatherTarget.NONE) {
      target = null;
    } else {
      leg = Leg.DELIVER;
    }
  }

  private void deliverLeg(final ServerWorld world) {
    if (chest == null || !reached(chest)) {
      return;
    }
    if (!ChestMarking.isErrandChest(world, chest)) {
      worker
          .owner()
          .ifPresent(
              owner ->
                  ChestMarking.forget(
                      world.getServer(), owner, GlobalPos.create(world.getRegistryKey(), chest)));
      leg = null;
      return;
    }
    final Inventory into = HopperBlockEntity.getInventoryAt(world, chest);
    if (into != null) {
      ErrandTransfer.deliverAll(worker.getInventory(), into);
    }
    leg = worker.getInventory().isEmpty() ? Leg.HARVEST : null;
  }

  @Nullable
  private BlockPos nextTarget(final ServerWorld world) {
    final int range = ServerConfigService.current().servants().gatherRange();
    for (final BlockPos pos : BlockPos.iterateOutwards(place(), range, range, range)) {
      if (!unreachable.contains(pos) && Harvest.kind(world, pos) != GatherTarget.NONE) {
        return pos.toImmutable();
      }
    }
    return null;
  }

  private boolean reached(final BlockPos block) {
    if (worker.getBlockPos().isWithinDistance(block, REACH)) {
      return true;
    }
    if (ticksOnTarget % 20 == 1 || worker.getNavigation().isIdle()) {
      worker
          .getNavigation()
          .startMovingTo(block.getX() + 0.5, block.getY(), block.getZ() + 0.5, SPEED);
    }
    return false;
  }
}
