package com.grahambartley.nightborn.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class DataEntries {

  private DataEntries() {}

  public static <T> Map<Identifier, T> copy(
      final DynamicRegistryManager registries, final RegistryKey<Registry<T>> key) {
    return copy(registries.getOptional(key));
  }

  public static <T> Map<Identifier, T> copy(final Optional<Registry<T>> registry) {
    final Map<Identifier, T> entries = new HashMap<>();
    registry.ifPresent(found -> found.getIds().forEach(id -> entries.put(id, found.get(id))));
    return entries;
  }
}
