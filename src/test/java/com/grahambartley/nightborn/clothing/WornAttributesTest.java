package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class WornAttributesTest {

  @ParameterizedTest(name = "{0}: {1} in {2}..{3}")
  @CsvSource({
    "notice_distance, 1.0, 0.2, 1.0",
    "hunter_sight, 1.0, 1.0, 1.5",
    "vanish_reach, 0.0, 0.0, 8.0",
    "ward_resistance, 0.0, 0.0, 0.8",
    "blood_drain, 1.0, 0.2, 3.0",
    "sun_damage, 1.0, 1.0, 3.0"
  })
  @DisplayName("each attribute starts neutral, and no reduction passes 80%")
  void bounds(final String path, final double base, final double min, final double max) {
    final RegistryEntry<EntityAttribute> attribute =
        WornAttributes.ALL.stream()
            .filter(a -> a.getKey().orElseThrow().getValue().getPath().equals(path))
            .findFirst()
            .orElseThrow();
    final ClampedEntityAttribute clamped = (ClampedEntityAttribute) attribute.value();
    assertEquals(base, clamped.getDefaultValue());
    assertEquals(min, clamped.getMinValue());
    assertEquals(max, clamped.getMaxValue());
    assertEquals("attribute.name.nightborn." + path, clamped.getTranslationKey());
  }
}
