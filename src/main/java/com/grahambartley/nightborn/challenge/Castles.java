package com.grahambartley.nightborn.challenge;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;

public final class Castles {

  public static final TagKey<Structure> CASTLES =
      TagKey.of(RegistryKeys.STRUCTURE, Identifier.of(Nightborn.MOD_ID, "castles"));

  private Castles() {}

  public static boolean contains(final ServerWorld world, final BlockPos pos) {
    return world.isPosLoaded(pos.getX(), pos.getZ())
        && world.getStructureAccessor().getStructureContaining(pos, CASTLES).hasChildren();
  }
}
