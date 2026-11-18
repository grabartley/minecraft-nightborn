package com.grahambartley.nightborn.servant.job;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.math.BlockPos;

public record JobOrder(DayJob job, BlockPos place, Optional<ScoutTrip> trip) {

  public static final Codec<JobOrder> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      DayJob.CODEC.fieldOf("job").forGetter(JobOrder::job),
                      BlockPos.CODEC.fieldOf("place").forGetter(JobOrder::place),
                      ScoutTrip.CODEC.optionalFieldOf("trip").forGetter(JobOrder::trip))
                  .apply(instance, JobOrder::new));

  public static JobOrder at(final DayJob job, final BlockPos place) {
    return new JobOrder(job, place.toImmutable(), Optional.empty());
  }

  public static JobOrder scout(final BlockPos from, final ScoutTrip trip) {
    return new JobOrder(DayJob.SCOUT, from.toImmutable(), Optional.of(trip));
  }

  public JobOrder withTrip(final ScoutTrip updated) {
    return new JobOrder(job, place, Optional.of(updated));
  }

  public boolean is(final DayJob other) {
    return job == other;
  }
}
