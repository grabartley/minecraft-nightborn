package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class JobOrderTest {

  static Stream<JobOrder> orders() {
    return Stream.of(
        JobOrder.at(DayJob.GATHER, new BlockPos(4, 70, 4)),
        JobOrder.at(DayJob.STAND_WATCH, new BlockPos(-8, 12, 30)),
        JobOrder.scout(
            new BlockPos(1, 64, 1),
            ScoutTrip.to(new BlockPos(900, 0, 2), Identifier.ofVanilla("village_taiga"))));
  }

  @ParameterizedTest
  @MethodSource("orders")
  @DisplayName("an order survives a save and load")
  void codec(final JobOrder order) {
    assertEquals(
        order,
        JobOrder.CODEC
            .parse(NbtOps.INSTANCE, JobOrder.CODEC.encodeStart(NbtOps.INSTANCE, order).getOrThrow())
            .getOrThrow());
  }

  @ParameterizedTest
  @EnumSource(DayJob.class)
  @DisplayName("an order is only the job it was given")
  void is(final DayJob job) {
    final JobOrder order = JobOrder.at(job, BlockPos.ORIGIN);
    for (final DayJob other : DayJob.values()) {
      assertEquals(other == job, order.is(other));
    }
    assertEquals(Optional.empty(), order.trip());
  }

  @ParameterizedTest
  @MethodSource("orders")
  @DisplayName("a new leg of a trip keeps the job and the place it was given")
  void withTrip(final JobOrder order) {
    final ScoutTrip trip = ScoutTrip.to(BlockPos.ORIGIN, Identifier.of("nightborn", "crypt"));
    final JobOrder moved = order.withTrip(trip);
    assertEquals(order.job(), moved.job());
    assertEquals(order.place(), moved.place());
    assertTrue(moved.trip().isPresent());
  }
}
