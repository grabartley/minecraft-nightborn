package com.grahambartley.nightborn.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.Lifecycle;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DataEntriesTest {

  private static final RegistryKey<Registry<String>> WORDS =
      RegistryKey.ofRegistry(Identifier.of("nightborn", "test_words"));

  private static DynamicRegistryManager registries(final List<String> words) {
    final SimpleRegistry<String> registry = new SimpleRegistry<>(WORDS, Lifecycle.stable());
    words.forEach(word -> Registry.register(registry, Identifier.of("nightborn", word), word));
    registry.freeze();
    return new DynamicRegistryManager.ImmutableImpl(List.of(registry));
  }

  @ParameterizedTest(name = "[{0}]")
  @CsvSource({"''", "grave", "grave;shroud;bone"})
  void copiesEveryEntryById(final String words) {
    final List<String> listed = words.isEmpty() ? List.of() : Arrays.asList(words.split(";"));
    final Map<Identifier, String> expected =
        listed.stream()
            .collect(Collectors.toMap(word -> Identifier.of("nightborn", word), word -> word));
    assertEquals(expected, DataEntries.copy(registries(listed), WORDS));
  }

  @ParameterizedTest(name = "missing registry")
  @CsvSource({"nightborn:absent"})
  void aMissingRegistryCopiesNothing(final String id) {
    final RegistryKey<Registry<String>> absent = RegistryKey.ofRegistry(Identifier.of(id));
    assertEquals(Map.of(), DataEntries.copy(registries(List.of("grave")), absent));
    assertEquals(Map.of(), DataEntries.copy(Optional.<Registry<String>>empty()));
  }
}
