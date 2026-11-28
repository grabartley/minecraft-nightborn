package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.Nightborn;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class MobActions {

  public static final RegistryKey<Registry<MobAction>> ACTION =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "mob_action"));

  private MobActions() {}

  public static void register() {
    DynamicRegistries.register(ACTION, MobAction.CODEC);
  }

  public static Optional<MobAction> get(
      final DynamicRegistryManager registries, final Identifier id) {
    return registries.getOptional(ACTION).flatMap(registry -> registry.getOrEmpty(id));
  }

  public static Map<Identifier, MobAction> all(final DynamicRegistryManager registries) {
    final Map<Identifier, MobAction> all = new TreeMap<>();
    registries
        .getOptional(ACTION)
        .ifPresent(registry -> registry.getIds().forEach(id -> all.put(id, registry.get(id))));
    return all;
  }
}
