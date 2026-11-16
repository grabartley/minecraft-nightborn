package com.grahambartley.nightborn.mind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.mob.VampireEntity;
import java.util.stream.Stream;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class MindAttributesTest {

  static Stream<Arguments> attributes() {
    return Stream.of(
        Arguments.of(MindAttributes.CHARM, "charm", 5.0),
        Arguments.of(MindAttributes.THRALL_LIMIT, "thrall_limit", 16.0));
  }

  @ParameterizedTest(name = "{1}")
  @MethodSource("attributes")
  @DisplayName("each mind attribute is registered, starts at zero and clamps to its range")
  void registered(
      final RegistryEntry<EntityAttribute> attribute, final String path, final double max) {
    assertEquals("nightborn:" + path, attribute.getIdAsString());
    assertEquals(MindAttributes.translationKey(path), attribute.value().getTranslationKey());
    assertEquals(0.0, attribute.value().getDefaultValue());
    assertEquals(0.0, attribute.value().clamp(-1.0));
    assertEquals(max, attribute.value().clamp(max + 1.0));
  }

  @ParameterizedTest(name = "{1}")
  @MethodSource("attributes")
  @DisplayName("players and vampire mobs carry the mind attributes their skills raise")
  void carried(final RegistryEntry<EntityAttribute> attribute, final String path) {
    final DefaultAttributeContainer players = PlayerEntity.createPlayerAttributes().build();
    final DefaultAttributeContainer vampires = VampireEntity.createVampireAttributes().build();
    assertTrue(players.has(attribute), path);
    assertTrue(vampires.has(attribute), path);
  }
}
