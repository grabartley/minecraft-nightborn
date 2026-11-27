package com.grahambartley.nightborn.guild.rite;

import com.grahambartley.nightborn.structure.Threshold;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public record RiteWay(BlockPos entrance, Function<ServerWorld, Optional<Threshold>> door) {}
