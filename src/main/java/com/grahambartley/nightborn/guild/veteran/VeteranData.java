package com.grahambartley.nightborn.guild.veteran;

import com.grahambartley.nightborn.Nightborn;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class VeteranData {

  public static final RegistryKey<Registry<Veteran>> VETERAN =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "veteran"));
  public static final RegistryKey<Registry<VeteranAttack>> ATTACK =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "veteran_attack"));

  private VeteranData() {}

  static void register() {
    DynamicRegistries.register(VETERAN, Veteran.CODEC);
    DynamicRegistries.register(ATTACK, VeteranAttack.CODEC);
  }

  public static Optional<Veteran> veteran(
      final DynamicRegistryManager registries, final Identifier id) {
    return registries.getOptional(VETERAN).map(registry -> registry.get(id));
  }

  public static Map<Identifier, VeteranAttack> attacks(
      final DynamicRegistryManager registries, final Veteran veteran) {
    final Map<Identifier, VeteranAttack> attacks = new LinkedHashMap<>();
    registries
        .getOptional(ATTACK)
        .ifPresent(
            registry ->
                veteran.attacks().stream()
                    .filter(registry::containsId)
                    .forEach(id -> attacks.put(id, registry.get(id))));
    return attacks;
  }
}
