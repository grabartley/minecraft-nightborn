package com.grahambartley.nightborn.thrall;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ThrallLookTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"desert", "jungle", "plains", "savanna", "snow", "swamp", "taiga"})
  @DisplayName("every villager type has a thrall overlay at its own path")
  void texture(final String type) {
    final VillagerType villagerType = Registries.VILLAGER_TYPE.get(Identifier.ofVanilla(type));
    final Identifier texture = ThrallLook.texture(villagerType);
    assertEquals("nightborn:textures/entity/thrall/" + type + ".png", texture.toString());
    assertTrue(
        Files.exists(Path.of("src/main/resources/assets/nightborn/" + texture.getPath())),
        texture.toString());
  }
}
