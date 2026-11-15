package com.grahambartley.nightborn.ammo;

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
class AmmoEntitiesTest {

  static Stream<Arguments> types() {
    return Stream.of(
        Arguments.of("bolt", AmmoEntities.BOLT, EntityType.ARROW),
        Arguments.of("garlic_bomb", AmmoEntities.GARLIC_BOMB, EntityType.SNOWBALL));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("types")
  @DisplayName("each projectile is registered and sized and tracked like its vanilla kin")
  void registered(final String name, final EntityType<?> type, final EntityType<?> kin) {
    assertEquals("nightborn:" + name, Registries.ENTITY_TYPE.getId(type).toString());
    assertEquals(SpawnGroup.MISC, type.getSpawnGroup());
    assertEquals(kin.getDimensions().width(), type.getDimensions().width());
    assertEquals(kin.getDimensions().height(), type.getDimensions().height());
    assertEquals(kin.getDimensions().eyeHeight(), type.getDimensions().eyeHeight());
    assertEquals(kin.getMaxTrackDistance(), type.getMaxTrackDistance());
    assertEquals(kin.getTrackTickInterval(), type.getTrackTickInterval());
  }
}
