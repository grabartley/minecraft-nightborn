package com.grahambartley.nightborn.raid;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public record Trail(
    UUID quarry,
    Optional<BlockPos> coffin,
    RegistryKey<World> world,
    long until,
    boolean endsAtDeath) {}
