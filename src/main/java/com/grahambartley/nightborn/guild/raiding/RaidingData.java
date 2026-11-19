package com.grahambartley.nightborn.guild.raiding;

import com.grahambartley.nightborn.Nightborn;
import java.util.Optional;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class RaidingData {

  public static final RegistryKey<Registry<RaidArms>> ARMS =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "raiding_party"));

  private RaidingData() {}

  static void register() {
    DynamicRegistries.register(ARMS, RaidArms.CODEC);
  }

  public static Optional<RaidArms> arms(
      final DynamicRegistryManager registries, final Identifier stage) {
    return registries
        .getOptional(ARMS)
        .flatMap(
            registry -> registry.stream().filter(arms -> arms.stage().equals(stage)).findFirst());
  }
}
