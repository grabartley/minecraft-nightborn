package com.grahambartley.nightborn.familiar;

import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.VillagerData;
import org.jetbrains.annotations.Nullable;

public final class FamiliarRelease {

  private FamiliarRelease() {}

  @Nullable
  public static VillagerEntity release(final HumanFamiliarEntity familiar) {
    if (!(familiar.getWorld() instanceof ServerWorld world)) {
      return null;
    }
    familiar
        .owner()
        .ifPresent(owner -> FamiliarRegistry.of(world.getServer()).free(owner, familiar.getUuid()));
    for (final ItemStack stack : familiar.getInventory().clearToList()) {
      familiar.dropStack(stack);
    }
    final VillagerData data = familiar.getVillagerData();
    final VillagerEntity villager = familiar.convertTo(EntityType.VILLAGER, true);
    if (villager != null) {
      villager.setVillagerData(data);
    }
    return villager;
  }

  public static void freeOwner(final MinecraftServer server, final UUID owner) {
    FamiliarRegistry.of(server).freeOwner(owner);
  }
}
