package com.grahambartley.nightborn.vampire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class NightbornDamageTest {

  static Stream<RegistryKey<DamageType>> types() {
    return Stream.of(
        NightbornDamage.BITE,
        NightbornDamage.SUN,
        NightbornDamage.STAKE,
        NightbornDamage.VOMIT,
        NightbornDamage.LANCET,
        NightbornDamage.BLEEDING);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("types")
  @DisplayName("every damage type is this mod's and ships its data file")
  void shipsItsData(final RegistryKey<DamageType> type) {
    assertEquals("nightborn", type.getValue().getNamespace());
    assertTrue(
        Files.exists(
            Path.of(
                "src/main/resources/data/nightborn/damage_type/"
                    + type.getValue().getPath()
                    + ".json")));
  }
}
