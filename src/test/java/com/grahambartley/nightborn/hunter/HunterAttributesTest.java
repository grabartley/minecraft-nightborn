package com.grahambartley.nightborn.hunter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HunterAttributesTest {

  static Stream<Arguments> attributes() {
    return Stream.of(
        Arguments.of(HunterAttributes.BITE_RESISTANCE, "bite_resistance", 1.0),
        Arguments.of(HunterAttributes.MESMERISE_RESISTANCE, "mesmerise_resistance", 1.0),
        Arguments.of(HunterAttributes.FAITH, "faith", 10.0));
  }

  @ParameterizedTest(name = "{1}")
  @MethodSource("attributes")
  @DisplayName("each hunter attribute is registered, starts at zero and clamps to its range")
  void registered(
      final RegistryEntry<EntityAttribute> attribute, final String path, final double max) {
    assertEquals("nightborn:" + path, attribute.getIdAsString());
    assertEquals(HunterAttributes.translationKey(path), attribute.value().getTranslationKey());
    assertEquals(0.0, attribute.value().getDefaultValue());
    assertEquals(0.0, attribute.value().clamp(-5.0));
    assertEquals(max, attribute.value().clamp(max + 5.0));
  }

  @ParameterizedTest(name = "{1}")
  @MethodSource("attributes")
  @DisplayName("every player carries the hunter attributes")
  void onPlayers(final RegistryEntry<EntityAttribute> attribute, final String path) {
    final DefaultAttributeContainer players = PlayerEntity.createPlayerAttributes().build();
    assertTrue(players.has(attribute), path);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"faith, attribute.name.nightborn.faith", "x, attribute.name.nightborn.x"})
  @DisplayName("names follow the vanilla attribute key pattern")
  void keys(final String path, final String key) {
    assertEquals(key, HunterAttributes.translationKey(path));
  }
}
