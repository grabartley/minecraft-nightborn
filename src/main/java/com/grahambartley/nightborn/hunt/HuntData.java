package com.grahambartley.nightborn.hunt;

import com.grahambartley.nightborn.Nightborn;
import java.util.List;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class HuntData {

  public static final RegistryKey<Registry<NotorietyKill>> KILL =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "notoriety_kill"));

  private HuntData() {}

  static void register() {
    DynamicRegistries.register(KILL, NotorietyKill.CODEC);
  }

  public static List<NotorietyKill> kills(final DynamicRegistryManager registries) {
    return registries
        .getOptional(KILL)
        .map(registry -> registry.stream().toList())
        .orElse(List.of());
  }
}
