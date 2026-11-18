package com.grahambartley.nightborn.servant.job.goal;

import com.grahambartley.nightborn.servant.job.DayJob;
import com.grahambartley.nightborn.servant.job.DayWorker;
import com.grahambartley.nightborn.servant.job.GatherTarget;
import com.grahambartley.nightborn.servant.job.Harvest;
import com.grahambartley.nightborn.servant.job.JobSupplies;
import com.grahambartley.nightborn.servant.job.RoundRules;
import com.grahambartley.nightborn.servant.job.RoundRules.Step;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class GatherGoal<T extends PathAwareEntity & DayWorker> extends ChestRoundGoal<T> {

  static final int PICKAXES = 1;

  private final Set<BlockPos> skipped = new HashSet<>();
  @Nullable private BlockPos target;

  public GatherGoal(final T worker) {
    super(worker, DayJob.GATHER);
  }

  @Override
  public void start() {
    skipped.clear();
    super.start();
  }

  @Override
  protected void stock(final Inventory chest) {
    JobSupplies.take(chest, worker.getInventory(), stack -> stack.isIn(Harvest.TOOLS), PICKAXES);
  }

  @Override
  protected boolean readyToWork() {
    return true;
  }

  @Override
  protected void forgetTarget() {
    target = null;
  }

  @Override
  protected Step work(final ServerWorld world) {
    if (target == null) {
      target = nextTarget(world);
      restartLegClock();
      if (target == null) {
        return Step.NOTHING_LEFT;
      }
    }
    if (RoundRules.stuck(ticksOnLeg())) {
      skip();
      return Step.WORKING;
    }
    final Vec3d spot = Vec3d.ofCenter(target);
    if (!walkTo(spot)) {
      return Step.WORKING;
    }
    worker.getLookControl().lookAt(spot.x, spot.y, spot.z);
    return switch (Harvest.reap(world, target, worker, worker.getInventory())) {
      case REAPED -> {
        worker.swingHand(Hand.MAIN_HAND);
        target = null;
        yield Step.WORKING;
      }
      case GONE -> {
        target = null;
        yield Step.WORKING;
      }
      case TOO_MUCH -> {
        skip();
        yield Step.WORKING;
      }
      case NO_ROOM -> Step.BACK;
    };
  }

  private void skip() {
    if (target != null) {
      skipped.add(target);
    }
    target = null;
  }

  @Nullable
  private BlockPos nextTarget(final ServerWorld world) {
    final int range = ServerConfigService.current().servants().gatherRange();
    for (final BlockPos pos : BlockPos.iterateOutwards(place(), range, range, range)) {
      if (skipped.contains(pos)
          || Harvest.kind(world, pos, worker.getInventory()) == GatherTarget.NONE) {
        continue;
      }
      final BlockPos base =
          world.getBlockState(pos).isIn(Harvest.WOOD)
              ? Harvest.base(world, pos)
              : pos.toImmutable();
      if (!skipped.contains(base)) {
        return base;
      }
    }
    return null;
  }
}
