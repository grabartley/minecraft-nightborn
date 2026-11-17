package com.grahambartley.nightborn.vampire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class VampireAttributesTest {

  static Stream<RegistryEntry<EntityAttribute>> all() {
    return VampireAttributes.ALL.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("all")
  @DisplayName("every attribute is this mod's, is sent to clients and starts within its bounds")
  void registered(final RegistryEntry<EntityAttribute> entry) {
    final ClampedEntityAttribute attribute = (ClampedEntityAttribute) entry.value();
    assertEquals("nightborn", entry.getKey().orElseThrow().getValue().getNamespace());
    assertTrue(attribute.isTracked());
    assertTrue(attribute.getDefaultValue() >= attribute.getMinValue());
    assertTrue(attribute.getDefaultValue() <= attribute.getMaxValue());
    assertEquals(
        VampireAttributes.translationKey(entry.getKey().orElseThrow().getValue().getPath()),
        attribute.getTranslationKey());
  }

  @ParameterizedTest(name = "{0} never falls below {1}")
  @CsvSource({"victim_struggle, 0.2", "downed_time, 0.2", "power_cooldown, 0.2"})
  @DisplayName("nothing a skill reduces is ever reduced by more than four fifths")
  void reductionsStopAtFourFifths(final String path, final double floor) {
    final ClampedEntityAttribute attribute =
        (ClampedEntityAttribute)
            all()
                .filter(e -> e.getKey().orElseThrow().getValue().getPath().equals(path))
                .findFirst()
                .orElseThrow()
                .value();
    assertEquals(floor, attribute.getMinValue());
    assertEquals(floor, attribute.clamp(0.0));
  }
}
