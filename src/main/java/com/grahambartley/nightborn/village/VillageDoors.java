package com.grahambartley.nightborn.village;

import com.grahambartley.nightborn.core.NightTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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

public final class VillageDoors {

  static final int DOOR_REACH = 6;
  static final int DOOR_HEIGHT = 2;
  static final double AT_HOME = 3.0;
  static final float HOME_SPEED = 0.6F;

  private VillageDoors() {}

  public static boolean shuts(final VillageStep step, final boolean night) {
    return night && step.atLeast(VillageStep.WARY);
  }

  public static boolean sendsHome(final boolean sleeping, final double squaredDistanceToHome) {
    return !sleeping && squaredDistanceToHome > AT_HOME * AT_HOME;
  }

  static void keep(
      final ServerWorld world, final VillageStep step, final List<VillagerEntity> villagers) {
    if (!shuts(
        step, NightTime.isNight(world.getDimension().hasFixedTime(), world.getTimeOfDay()))) {
      return;
    }
    for (final BlockPos door : doors(world, villagers)) {
      final BlockState state = world.getBlockState(door);
      if (state.getBlock() instanceof DoorBlock block && block.isOpen(state)) {
        block.setOpen(null, world, state, door, false);
      }
    }
    for (final VillagerEntity villager : villagers) {
      home(villager)
          .filter(
              home ->
                  sendsHome(villager.isSleeping(), villager.getBlockPos().getSquaredDistance(home)))
          .ifPresent(
              home ->
                  villager
                      .getBrain()
                      .remember(MemoryModuleType.WALK_TARGET, new WalkTarget(home, HOME_SPEED, 1)));
    }
  }

  static List<BlockPos> doors(final ServerWorld world, final List<VillagerEntity> villagers) {
    final Set<BlockPos> doors = new LinkedHashSet<>();
    for (final VillagerEntity villager : villagers) {
      home(villager)
          .ifPresent(
              home -> {
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
              });
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
