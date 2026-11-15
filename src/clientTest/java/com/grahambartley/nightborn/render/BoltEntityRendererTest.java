package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BoltEntityRendererTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"stake_bolt", "silver_bolt", "holy_water_bolt"})
  @DisplayName("a bolt in flight draws the projectile texture named after its item")
  void texture(final String bolt) {
    final Identifier texture = BoltEntityRenderer.texture(Identifier.of("nightborn", bolt));
    assertEquals("nightborn:textures/entity/projectiles/" + bolt + ".png", texture.toString());
    assertTrue(
        Files.exists(Path.of("src/main/resources/assets/nightborn").resolve(texture.getPath())));
  }
}
