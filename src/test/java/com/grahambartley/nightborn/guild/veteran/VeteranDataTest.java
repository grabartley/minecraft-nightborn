package com.grahambartley.nightborn.guild.veteran;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VeteranDataTest {

  private static final Path DATA = Path.of("src/main/resources/data/nightborn");

  private static RegistryKey<? extends Registry<?>> key(final String name) {
    return "veteran".equals(name) ? VeteranData.VETERAN : VeteranData.ATTACK;
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"veteran", "veteran_attack"})
  void readsAFolderTheModShips(final String name) {
    final RegistryKey<? extends Registry<?>> key = key(name);
    assertTrue(
        Files.isDirectory(
            DATA.resolve(key.getValue().getNamespace()).resolve(key.getValue().getPath())));
  }
}
