package com.grahambartley.nightborn.village;

import com.grahambartley.nightborn.core.NightTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import org.jetbrains.annotations.Nullable;

public final class VillageDoors {

  static final int DOOR_REACH = 6;
  static final int DOOR_HEIGHT = 2;
  static final double AT_HOME = 3.0;
  static final float HOME_SPEED = 0.6F;

  private static final Map<GlobalPos, Long> SHUT = new HashMap<>();

  private VillageDoors() {}

  public static boolean shutsTonight(final long night, @Nullable final Long lastShut) {
    return lastShut == null || lastShut != night;
  }

  public static boolean shuts(final VillageStep step, final boolean night) {
    return night && step.atLeast(VillageStep.WARY);
  }

  public static boolean sendsHome(
      final boolean sleeping, final boolean busy, final double squaredDistanceToHome) {
    return !sleeping && !busy && squaredDistanceToHome > AT_HOME * AT_HOME;
  }

  static void keep(
      final ServerWorld world,
      final BlockPos bell,
      final VillageStep step,
      final List<VillagerEntity> villagers) {
    if (!shuts(
        step, NightTime.isNight(world.getDimension().hasFixedTime(), world.getTimeOfDay()))) {
      return;
    }
    final long night = VillageSuspicion.night(world);
    if (shutsTonight(night, SHUT.get(GlobalPos.create(world.getRegistryKey(), bell)))) {
      SHUT.put(GlobalPos.create(world.getRegistryKey(), bell), night);
      for (final BlockPos door : doors(world, villagers)) {
        final BlockState state = world.getBlockState(door);
        if (state.getBlock() instanceof DoorBlock block && block.isOpen(state)) {
          block.setOpen(null, world, state, door, false);
        }
      }
    }
    for (final VillagerEntity villager : villagers) {
      home(villager)
          .filter(
              home ->
                  sendsHome(
                      villager.isSleeping(),
                      busy(world, villager),
                      villager.getBlockPos().getSquaredDistance(home)))
          .ifPresent(
              home ->
                  villager
                      .getBrain()
                      .remember(MemoryModuleType.WALK_TARGET, new WalkTarget(home, HOME_SPEED, 1)));
    }
  }

  private static boolean busy(final ServerWorld world, final VillagerEntity villager) {
    return WitnessReports.running(world, villager)
        || villager.getBrain().hasMemoryModule(MemoryModuleType.HURT_BY);
  }

  static void forget(final ServerWorld world, final BlockPos bell) {
    SHUT.remove(GlobalPos.create(world.getRegistryKey(), bell));
  }

  static void clear() {
    SHUT.clear();
  }

  static List<BlockPos> doors(final ServerWorld world, final List<VillagerEntity> villagers) {
    final Set<BlockPos> doors = new LinkedHashSet<>();
    final Set<BlockPos> homes = new LinkedHashSet<>();
    villagers.forEach(villager -> home(villager).ifPresent(homes::add));
    for (final BlockPos home : homes) {
      for (final BlockPos pos :
          BlockPos.iterate(
              home.add(-DOOR_REACH, -DOOR_HEIGHT, -DOOR_REACH),
              home.add(DOOR_REACH, DOOR_HEIGHT, DOOR_REACH))) {
        final BlockState state = world.getBlockState(pos);
        if (state.isIn(BlockTags.WOODEN_DOORS)
            && state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
          doors.add(pos.toImmutable());
        }
      }
    }
    return new ArrayList<>(doors);
  }

  private static Optional<BlockPos> home(final VillagerEntity villager) {
    return villager
        .getBrain()
        .getOptionalRegisteredMemory(MemoryModuleType.HOME)
        .filter(point -> point.dimension() == villager.getWorld().getRegistryKey())
        .map(GlobalPos::pos);
  }
}
