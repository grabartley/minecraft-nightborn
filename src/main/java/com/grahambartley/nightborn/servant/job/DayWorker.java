package com.grahambartley.nightborn.servant.job;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.player.PlayerEntity;

public interface DayWorker extends InventoryOwner {

  int CARRY_SLOTS = 8;

  Optional<UUID> owner();

  Optional<JobOrder> job();

  void assign(JobOrder order);

  void dismiss();

  default boolean willWork() {
    return true;
  }

  default boolean workingFor(final PlayerEntity player) {
    return owner().filter(player.getUuid()::equals).isPresent();
  }

  default boolean doing(final DayJob job) {
    return job().filter(order -> order.is(job)).isPresent();
  }
}
