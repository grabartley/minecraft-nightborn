package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class NightbornMobsTest {

  static Stream<Arguments> vampires() {
    return Stream.of(
        Arguments.of("nightborn:vampire", NightbornMobs.VAMPIRE),
        Arguments.of("nightborn:vampire_lord", NightbornMobs.VAMPIRE_LORD));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("vampires")
  @DisplayName("every vampire type is a registered monster with the shared vampire frame")
  void sharesTheVampireFrame(final String id, final EntityType<?> type) {
    assertEquals(id, Registries.ENTITY_TYPE.getId(type).toString());
    assertEquals(SpawnGroup.MONSTER, type.getSpawnGroup());
    assertEquals(NightbornMobs.VAMPIRE_WIDTH, type.getWidth());
    assertEquals(NightbornMobs.VAMPIRE_HEIGHT, type.getHeight());
    assertEquals(NightbornMobs.VAMPIRE_EYES, type.getDimensions().eyeHeight());
  }
}
