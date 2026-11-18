package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DayWorkerTest {

  private static final class Worker implements DayWorker {

    private final SimpleInventory carried = new SimpleInventory(CARRY_SLOTS);
    private JobOrder job;

    @Override
    public Optional<UUID> owner() {
      return Optional.empty();
    }

    @Override
    public Optional<JobOrder> job() {
      return Optional.ofNullable(job);
    }

    @Override
    public void assign(final JobOrder order) {
      job = order;
    }

    @Override
    public void dismiss() {
      job = null;
    }

    @Override
    public SimpleInventory getInventory() {
      return carried;
    }
  }

  @ParameterizedTest(name = "doing {0}, asked about {1} -> {2}")
  @CsvSource({
    "GATHER, GATHER, true",
    "GATHER, SCOUT, false",
    "STAND_WATCH, STAND_WATCH, true",
    ", GATHER, false"
  })
  @DisplayName("a worker is doing a job only while that job is its order")
  void doing(final DayJob given, final DayJob asked, final boolean doing) {
    final Worker worker = new Worker();
    if (given != null) {
      worker.assign(JobOrder.at(given, BlockPos.ORIGIN));
    }
    assertEquals(doing, worker.doing(asked));
    worker.dismiss();
    assertEquals(false, worker.doing(asked));
    assertEquals(true, worker.willWork());
  }
}
