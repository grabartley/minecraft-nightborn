package com.grahambartley.nightborn.dismember;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class BodyZoneData {

  public static final RegistryKey<Registry<BodyZones>> ZONES =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "body_zones"));

  static final Identifier HUMANOID = Identifier.of(Nightborn.MOD_ID, "humanoid");

  private BodyZoneData() {}

  public static void register() {
    DynamicRegistries.register(ZONES, BodyZones.CODEC);
  }

  public static BodyZones zones(final DynamicRegistryManager registries) {
    return registries
        .getOptional(ZONES)
        .map(registry -> registry.get(HUMANOID))
        .orElse(BodyZones.DEFAULT);
  }
}
