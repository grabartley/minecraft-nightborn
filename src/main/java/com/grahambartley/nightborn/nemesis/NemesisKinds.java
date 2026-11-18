package com.grahambartley.nightborn.nemesis;

import com.grahambartley.nightborn.Nightborn;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.entity.Entity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class NemesisKinds {

  public static final RegistryKey<Registry<NemesisKind>> KIND =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "nemesis_kind"));

  public record Found(Identifier id, NemesisKind kind) {}

  private NemesisKinds() {}

  static void register() {
    DynamicRegistries.register(KIND, NemesisKind.CODEC);
  }

  public static Optional<Found> of(final Entity entity) {
    return find(all(entity.getRegistryManager()), Registries.ENTITY_TYPE.getId(entity.getType()));
  }

  public static Optional<NemesisKind> byId(
      final DynamicRegistryManager registries, final Identifier id) {
    return Optional.ofNullable(all(registries).get(id));
  }

  public static Optional<Found> find(
      final Map<Identifier, NemesisKind> kinds, final Identifier entity) {
    return kinds.entrySet().stream()
        .filter(entry -> entry.getValue().entity().equals(entity))
        .min(Map.Entry.comparingByKey())
        .map(entry -> new Found(entry.getKey(), entry.getValue()));
  }

  static Map<Identifier, NemesisKind> all(final DynamicRegistryManager registries) {
    final Map<Identifier, NemesisKind> kinds = new LinkedHashMap<>();
    registries
        .getOptional(KIND)
        .ifPresent(registry -> registry.getIds().forEach(id -> kinds.put(id, registry.get(id))));
    return kinds;
  }
}
