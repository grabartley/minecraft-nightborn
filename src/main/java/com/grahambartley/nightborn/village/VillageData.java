package com.grahambartley.nightborn.village;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class VillageData {

  public static final RegistryKey<Registry<SuspicionSource>> SOURCE =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "suspicion_source"));

  private VillageData() {}

  static void register() {
    DynamicRegistries.register(SOURCE, SuspicionSource.CODEC);
  }

  public static int amount(final DynamicRegistryManager registries, final SuspicionCause cause) {
    return registries
        .getOptional(SOURCE)
        .map(registry -> registry.get(cause.id()))
        .map(SuspicionSource::amount)
        .orElse(0);
  }
}
