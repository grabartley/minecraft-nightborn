package com.grahambartley.nightborn.house;

import java.util.UUID;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

final class HouseFixtures {

  static final UUID LORD = UUID.fromString("00000000-0000-0000-0000-000000000001");
  static final UUID PLAYER = UUID.fromString("00000000-0000-0000-0000-000000000002");
  static final UUID NPC = UUID.fromString("00000000-0000-0000-0000-000000000003");
  static final UUID STRANGER = UUID.fromString("00000000-0000-0000-0000-000000000004");

  private HouseFixtures() {}

  static GlobalPos at(final RegistryKey<World> world, final int x, final int y, final int z) {
    return GlobalPos.create(world, new BlockPos(x, y, z));
  }

  static GlobalPos at(final int x, final int y, final int z) {
    return at(World.OVERWORLD, x, y, z);
  }
}
