package com.grahambartley.nightborn.raid;

import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class HouseGuardGoal extends TrackTargetGoal {

  static final int RETHINK_TICKS = 10;

  private final Supplier<Optional<BlockPos>> house;
  private final BooleanSupplier onDuty;
  private final Runnable alarm;
  @Nullable private LivingEntity seen;
  private int rethink;

  public HouseGuardGoal(
      final MobEntity guard,
      final Supplier<Optional<BlockPos>> house,
      final BooleanSupplier onDuty,
      final Runnable alarm) {
    super(guard, true);
    this.house = house;
    this.onDuty = onDuty;
    this.alarm = alarm;
    setControls(EnumSet.of(Control.TARGET));
  }

  @Override
  public boolean canStart() {
    if (--rethink > 0 || !(mob.getWorld() instanceof ServerWorld world)) {
      return false;
    }
    rethink = RETHINK_TICKS;
    if (mob.getTarget() != null || house.get().isEmpty() || !onDuty.getAsBoolean()) {
      return false;
    }
    final int range = ServerConfigService.current().servants().watchRange();
    seen =
        world.getClosestEntity(
            LivingEntity.class,
            TargetPredicate.createAttackable()
                .setBaseMaxDistance(range)
                .setPredicate(HouseAlarm::intruder),
            mob,
            mob.getX(),
            mob.getEyeY(),
            mob.getZ(),
            mob.getBoundingBox().expand(range));
    return seen != null;
  }

  @Override
  public void start() {
    if (seen != null && mob.getWorld() instanceof ServerWorld world) {
      house.get().ifPresent(home -> HouseAlarm.raise(world, home, seen));
      alarm.run();
      mob.setTarget(seen);
    }
    super.start();
  }

  @Override
  protected double getFollowRange() {
    return ServerConfigService.current().servants().watchRange();
  }
}
