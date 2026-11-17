package com.grahambartley.nightborn.structure;

import java.util.Optional;
import net.minecraft.util.math.BlockPos;

public interface HouseholdMember {

  void settleAt(BlockPos home);

  Optional<BlockPos> home();
}
