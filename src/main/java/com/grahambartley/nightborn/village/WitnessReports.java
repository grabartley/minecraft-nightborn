package com.grahambartley.nightborn.village;

import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class WitnessReports {

  static final int CHECK_TICKS = 20;
  static final float RUN_SPEED = 0.7F;

  private static final Map<RegistryKey<World>, List<WitnessReport>> RUNNING = new HashMap<>();
  private static final Map<RegistryKey<World>, List<WitnessReport>> TOLD = new HashMap<>();

  private WitnessReports() {}

  static void register() {
    ServerTickEvents.END_WORLD_TICK.register(WitnessReports::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> clear());
  }

  public static boolean saw(
      final ServerWorld world,
      final VillagerEntity witness,
      final Entity vampire,
      final SuspicionCause cause) {
    final Optional<BlockPos> bell = VillageSuspicion.bellOf(witness);
    if (bell.isEmpty()
        || vampire instanceof PlayerEntity player
            && !WitnessReport.reportable(player.isCreative(), player.isSpectator())) {
      return false;
    }
    final int reportTicks = reportTicks();
    final List<WitnessReport> told = told(world, reportTicks);
    final WitnessReport report =
        new WitnessReport(witness.getUuid(), vampire.getUuid(), cause, bell.get(), world.getTime());
    final List<WitnessReport> running =
        RUNNING.computeIfAbsent(world.getRegistryKey(), key -> new ArrayList<>());
    if (told.stream().anyMatch(other -> report.sameEvent(other, reportTicks))
        || running.stream().anyMatch(report::sameSighting)) {
      return false;
    }
    running.add(report);
    return true;
  }

  private static void tick(final ServerWorld world) {
    final List<WitnessReport> running = RUNNING.get(world.getRegistryKey());
    if (running == null || running.isEmpty() || world.getTime() % CHECK_TICKS != 0) {
      return;
    }
    final int reportTicks = reportTicks();
    final Iterator<WitnessReport> reports = running.iterator();
    while (reports.hasNext()) {
      final WitnessReport report = reports.next();
      final VillagerEntity witness =
          world.getEntity(report.witness()) instanceof VillagerEntity villager ? villager : null;
      final WitnessReport.Outcome outcome =
          report.outcome(
              witness != null && witness.isAlive(),
              witness == null
                  ? Double.MAX_VALUE
                  : witness.getBlockPos().getSquaredDistance(report.bell()),
              world.getTime(),
              reportTicks);
      if (outcome == WitnessReport.Outcome.RUNNING) {
        runForBell(witness, report.bell());
        continue;
      }
      reports.remove();
      if (outcome == WitnessReport.Outcome.TOLD) {
        tell(world, witness, report);
      }
    }
  }

  private static void runForBell(final VillagerEntity witness, final BlockPos bell) {
    final Brain<?> brain = witness.getBrain();
    brain.forget(MemoryModuleType.HURT_BY);
    brain.forget(MemoryModuleType.HURT_BY_ENTITY);
    brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(bell, RUN_SPEED, 1));
  }

  private static void tell(
      final ServerWorld world, final VillagerEntity witness, final WitnessReport report) {
    final int reportTicks = reportTicks();
    final List<WitnessReport> told = told(world, reportTicks);
    if (told.stream().anyMatch(other -> report.sameEvent(other, reportTicks))) {
      return;
    }
    told.add(report);
    ringBell(world, witness, report.bell());
    VillageSuspicion.raise(world, report.bell(), report.cause(), Optional.of(report.vampire()));
  }

  private static void ringBell(
      final ServerWorld world, final VillagerEntity witness, final BlockPos bell) {
    if (!world.isChunkLoaded(bell)) {
      return;
    }
    final BlockState state = world.getBlockState(bell);
    if (state.getBlock() instanceof BellBlock block) {
      block.ring(witness, world, bell, null);
    }
  }

  public static boolean running(final ServerWorld world, final VillagerEntity witness) {
    return RUNNING.getOrDefault(world.getRegistryKey(), List.of()).stream()
        .anyMatch(report -> report.witness().equals(witness.getUuid()));
  }

  private static List<WitnessReport> told(final ServerWorld world, final int reportTicks) {
    final List<WitnessReport> told =
        TOLD.computeIfAbsent(world.getRegistryKey(), key -> new ArrayList<>());
    told.removeIf(report -> report.stale(world.getTime(), reportTicks * 2));
    return told;
  }

  private static int reportTicks() {
    return ServerConfigService.current().villages().reportSeconds() * 20;
  }

  public static void clear() {
    RUNNING.clear();
    TOLD.clear();
  }
}
