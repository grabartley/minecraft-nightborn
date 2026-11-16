package com.grahambartley.nightborn.familiar;

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
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class FamiliarLiveryTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({"desert", "jungle", "plains", "savanna", "snow", "swamp", "taiga"})
  @DisplayName("every villager biome type has a livery texture in the mod")
  void texture(final String type) {
    final VillagerType villagerType = Registries.VILLAGER_TYPE.get(Identifier.ofVanilla(type));
    assertEquals(
        "nightborn:textures/entity/human_familiar/" + type + ".png",
        FamiliarLivery.texture(villagerType).toString());
    assertTrue(
        Files.exists(
            Path.of(
                "src/main/resources/assets/nightborn/textures/entity/human_familiar/"
                    + type
                    + ".png")));
  }
}
