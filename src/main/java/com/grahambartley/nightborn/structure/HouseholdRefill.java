package com.grahambartley.nightborn.structure;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.StructureSettings;
import com.grahambartley.nightborn.house.Houses;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

public final class HouseholdRefill {

  static final int SWEEP_TICKS = 1200;

  private HouseholdRefill() {}

  public static void register() {
    ServerTickEvents.END_WORLD_TICK.register(
        world -> {
          if (world.getTime() % SWEEP_TICKS == 0) {
            sweep(world);
          }
        });
  }

  public static void sweep(final ServerWorld world) {
    final HouseholdRoll roll = HouseholdRoll.get(world.getServer());
    final StructureSettings settings = ServerConfigService.current().structures();
    final long now = world.getTimeOfDay();
    roll.records()
        .forEach(
            (home, record) -> {
              if (!home.dimension().equals(world.getRegistryKey())) {
                return;
              }
              if (RefillClock.redraws(record, now, settings.refillMaxDays())) {
                roll.refillAt(
                    home,
                    RefillClock.drawn(
                        now,
                        settings.refillMinDays(),
                        settings.refillMaxDays(),
                        world.getRandom()::nextInt));
              } else if (record.refillDue(now)) {
                arrive(world, home.pos());
              }
            });
  }

  public static boolean mayArrive(final ServerWorld world, final BlockPos home) {
    final GlobalPos at = GlobalPos.create(world.getRegistryKey(), home);
    final HouseholdRoll roll = HouseholdRoll.get(world.getServer());
    return RefillClock.mayArrive(
        roll.known(at), roll.record(at), Houses.get(world.getServer()).holding(at).isPresent());
  }

  public static boolean arrive(final ServerWorld world, final BlockPos home) {
    if (!mayArrive(world, home)) {
      return false;
    }
    final Optional<StructureStart> start = VampireStructureStarts.startAt(world, home);
    final Optional<BlockBox> heart = start.flatMap(found -> StructureHeart.of(found.getChildren()));
    if (start.isEmpty()
        || heart.isEmpty()
        || !VampireStructureStarts.whollyLoaded(world, heart.get())) {
      return false;
    }
    final VampireStructure structure = (VampireStructure) start.get().getStructure();
    final List<Entity> arrivals =
        HouseholdSpawner.settle(
            world, heart.get(), heart.get(), structure.household().returning(), world.getRandom());
    final Set<UUID> living = arrivals.stream().map(Entity::getUuid).collect(Collectors.toSet());
    HouseholdRoll.get(world.getServer())
        .refilled(GlobalPos.create(world.getRegistryKey(), home), living);
    Nightborn.LOGGER.debug("A new household of {} moved in at {}", living.size(), home);
    return true;
  }
}
