package com.grahambartley.nightborn.house.duel;

import com.grahambartley.nightborn.boss.Arena;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public record HouseDuel(UUID lord, UUID challenger, RegistryKey<World> world, Arena arena) {

  public boolean involves(final UUID fighter) {
    return lord.equals(fighter) || challenger.equals(fighter);
  }

  public Optional<UUID> foe(final UUID fighter) {
    if (lord.equals(fighter)) {
      return Optional.of(challenger);
    }
    return challenger.equals(fighter) ? Optional.of(lord) : Optional.empty();
  }
}
