package com.grahambartley.nightborn.servant.job;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.coffin.AssignedCoffin;
import com.grahambartley.nightborn.servant.OrderCue;
import com.grahambartley.nightborn.wheel.WheelAction;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final class DayJobOrders<T extends PathAwareEntity & DayWorker> implements WheelAction<T> {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "day_job");

  @Override
  public Identifier id() {
    return ID;
  }

  @Override
  public boolean allows(final ServerPlayerEntity player, final T worker) {
    return worker.workingFor(player);
  }

  @Override
  public List<WheelEntry> entries(final ServerPlayerEntity player, final T worker) {
    final List<WheelEntry> entries = new ArrayList<>();
    for (final DayJob job : List.of(DayJob.GATHER, DayJob.FETCH_BLOOD)) {
      if (place(player, worker, job).isPresent()) {
        entries.add(entry(worker, job));
      }
    }
    if (place(player, worker, DayJob.STAND_WATCH).isPresent()) {
      entries.add(entry(worker, DayJob.STAND_WATCH));
    }
    return entries;
  }

  private WheelEntry entry(final T worker, final DayJob job) {
    return new WheelEntry(
        ID, job.ordinal(), job.icon(), Text.translatable(job.translationKey()), worker.doing(job));
  }

  @Override
  public void perform(final ServerPlayerEntity player, final T worker, final WheelEntry chosen) {
    final DayJob job = DayJob.byIndex(chosen.argument());
    place(player, worker, job)
        .ifPresent(
            place -> {
              worker.assign(JobOrder.at(job, place));
              OrderCue.obey(
                  worker, player, Text.translatable(job.orderedKey(), worker.getDisplayName()));
            });
  }

  private Optional<BlockPos> place(
      final ServerPlayerEntity player, final T worker, final DayJob job) {
    if (!(worker.getWorld() instanceof ServerWorld world)) {
      return Optional.empty();
    }
    return switch (job) {
      case GATHER, FETCH_BLOOD ->
          Optional.of(worker.getBlockPos().toImmutable())
              .filter(here -> JobChest.near(world, player.getUuid(), here).isPresent());
      case STAND_WATCH -> AssignedCoffin.in(player, world);
      default -> Optional.empty();
    };
  }
}
