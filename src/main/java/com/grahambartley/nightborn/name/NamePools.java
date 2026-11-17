package com.grahambartley.nightborn.name;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class NamePools {

  public static final RegistryKey<Registry<NamePool>> POOL =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "name_pool"));

  private NamePools() {}

  public static void register() {
    DynamicRegistries.register(POOL, NamePool.CODEC);
  }

  public static NamePool pool(final DynamicRegistryManager registries, final Identifier id) {
    return registries
        .getOptional(POOL)
        .flatMap(registry -> registry.getOrEmpty(id))
        .orElse(NamePool.EMPTY);
  }
}
