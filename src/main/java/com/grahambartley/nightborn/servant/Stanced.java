package com.grahambartley.nightborn.servant;

import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface Stanced {

  Optional<Stance> stance();

  BlockPos post();

  @Nullable
  LivingEntity leaderHere();

  boolean guardsAgainst(LivingEntity intruder);

  default boolean holds(final Stance shown) {
    return stance().filter(shown::equals).isPresent();
  }
}
