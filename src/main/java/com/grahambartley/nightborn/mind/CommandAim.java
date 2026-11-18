package com.grahambartley.nightborn.mind;

import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

public record CommandAim(
    LivingEntity subject,
    CommandOrder order,
    Optional<LivingEntity> foe,
    Optional<BlockPos> door) {}
