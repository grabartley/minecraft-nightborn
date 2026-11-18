package com.grahambartley.nightborn.raid;

import com.grahambartley.nightborn.Nightborn;
import java.util.Optional;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class RaidData {

  public static final RegistryKey<Registry<NoiseKind>> NOISE =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "noise"));

  private RaidData() {}

  static void register() {
    DynamicRegistries.register(NOISE, NoiseKind.CODEC);
  }

  public static Optional<NoiseKind> noise(
      final DynamicRegistryManager registries, final NoiseAct act) {
    return registries
        .getOptional(NOISE)
        .flatMap(registry -> registry.getOrEmpty(Identifier.of(Nightborn.MOD_ID, act.asString())));
  }
}
