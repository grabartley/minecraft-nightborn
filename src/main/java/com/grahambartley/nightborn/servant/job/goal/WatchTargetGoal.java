package com.grahambartley.nightborn.servant.job.goal;

import com.grahambartley.nightborn.downed.DownedTargeting;
import com.grahambartley.nightborn.servant.ServantTargets;
import com.grahambartley.nightborn.servant.job.DayJob;
import com.grahambartley.nightborn.servant.job.DayWorker;
import com.grahambartley.nightborn.servant.job.JobOrder;
import com.grahambartley.nightborn.servant.job.WatchRules;
import com.grahambartley.nightborn.servant.job.WatchWarning;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class WatchTargetGoal<T extends PathAwareEntity & DayWorker> extends TrackTargetGoal {

  static final int RETHINK_TICKS = 10;

  private final T worker;
  @Nullable private LivingEntity chosen;
  private int rethink;
  private boolean sawHunters;

  public WatchTargetGoal(final T worker) {
    super(worker, false);
    this.worker = worker;
    setControls(EnumSet.of(Control.TARGET));
  }

  @Override
  public boolean canStart() {
    if (--rethink > 0 || !(worker.getWorld() instanceof ServerWorld world)) {
      return false;
    }
    rethink = RETHINK_TICKS;
    final Optional<BlockPos> post =
        worker.job().filter(order -> order.is(DayJob.STAND_WATCH)).map(JobOrder::place);
    if (post.isEmpty() || !worker.willWork()) {
      sawHunters = false;
      return false;
    }
    final List<LivingEntity> intruders = intruders(world, post.get());
    final boolean seesHunters =
        intruders.stream().anyMatch(foe -> foe.getType().isIn(DownedTargeting.HUNTERS));
    if (WatchRules.warns(seesHunters, sawHunters)) {
      WatchWarning.raise(world, worker);
    }
    sawHunters = seesHunters;
    chosen =
        intruders.stream()
            .filter(foe -> canTrack(foe, TargetPredicate.DEFAULT))
            .min(Comparator.comparingDouble(worker::squaredDistanceTo))
            .orElse(null);
    return chosen != null;
  }

  private List<LivingEntity> intruders(final ServerWorld world, final BlockPos post) {
    final int range = ServerConfigService.current().servants().watchRange();
    final Vec3d centre = Vec3d.ofBottomCenter(post);
    return world.getEntitiesByClass(
        LivingEntity.class,
        new Box(centre, centre).expand(range),
        living ->
            WatchRules.intrudes(
                    living instanceof Monster,
                    living.getType().isIn(DownedTargeting.HUNTERS),
                    WatchRules.spared(living instanceof CreeperEntity, Vampires.isVampire(living)))
                && WatchRules.watched(living.squaredDistanceTo(centre), range)
                && ServantTargets.mayAttack(worker, living));
  }

  @Override
  public void start() {
    worker.setTarget(chosen);
    super.start();
  }
}
