package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.mob.look.VampireRig;
import com.mojang.serialization.Lifecycle;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class RestlessDeadTest {

  private static RestlessLook look(final String id) {
    return new RestlessLook(
        VampireRig.FEMALE, Identifier.of("nightborn", "textures/" + id + ".png"), 1);
  }

  @ParameterizedTest(name = "[{0}]")
  @CsvSource({"''", "lady", "lady;shrouded;mourner"})
  void readsEveryLookFromItsRegistry(final String ids) {
    final List<String> listed = ids.isEmpty() ? List.of() : Arrays.asList(ids.split(";"));
    final SimpleRegistry<RestlessLook> registry =
        new SimpleRegistry<>(RestlessDead.LOOK, Lifecycle.stable());
    listed.forEach(id -> Registry.register(registry, Identifier.of("nightborn", id), look(id)));
    registry.freeze();
    final Map<Identifier, RestlessLook> expected =
        listed.stream()
            .collect(Collectors.toMap(id -> Identifier.of("nightborn", id), Function.identity()))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> look(entry.getValue())));
    assertEquals(
        expected, RestlessDead.looks(new DynamicRegistryManager.ImmutableImpl(List.of(registry))));
  }
}
