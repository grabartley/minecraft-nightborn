package com.grahambartley.nightborn.guild.build;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.registry.DynamicRegistryManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HunterBuildsTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"hunter_build"})
  @DisplayName("the build is read from its own data folder")
  void readsItsFolder(final String folder) {
    assertEquals(folder, HunterBuilds.BUILD.getValue().getPath());
    assertEquals("hunter", HunterBuilds.HUNTER.getPath());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"empty"})
  @DisplayName("with no build loaded, a hunter takes no weights and no growth")
  void noDataIsNoBuild(final String registries) {
    assertEquals(HunterBuild.NONE, HunterBuilds.build(DynamicRegistryManager.EMPTY));
  }
}
