package com.grahambartley.nightborn.mob.household;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.core.DataEntries;
import com.grahambartley.nightborn.mob.look.Weighted;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class Temperaments {

  public static final RegistryKey<Registry<Temperament>> TEMPERAMENT =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "vampire_temperament"));

  public static final Identifier FALLBACK = Identifier.of(Nightborn.MOD_ID, "even");

  private Temperaments() {}

  public static void register() {
    DynamicRegistries.register(TEMPERAMENT, Temperament.CODEC);
  }

  public static Optional<Identifier> pick(
      final Map<Identifier, Temperament> temperaments, final double roll) {
    return Weighted.pick(
            new TreeMap<>(temperaments).entrySet().stream().toList(),
            entry -> entry.getValue().weight(),
            roll)
        .map(Map.Entry::getKey);
  }

  public static Map<Identifier, Temperament> all(final DynamicRegistryManager registries) {
    return new TreeMap<>(DataEntries.copy(registries, TEMPERAMENT));
  }

  public static Temperament of(final DynamicRegistryManager registries, final Identifier id) {
    return registries
        .getOptional(TEMPERAMENT)
        .flatMap(registry -> registry.getOrEmpty(id))
        .orElse(Temperament.EVEN);
  }
}
