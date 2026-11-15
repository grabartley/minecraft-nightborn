package com.grahambartley.nightborn.coating;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.config.CoatingSettings;
import com.mojang.serialization.JsonOps;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CoatingTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "HOLY_WATER, holy_water, holy_water, block.fire.extinguish, white_smoke",
    "GARLIC, garlic, garlic_touch, entity.splash_potion.break, item"
  })
  @DisplayName("each coating weakens through its own source and fizzles with its own hit")
  void facts(
      final Coating coating,
      final String id,
      final String source,
      final String sound,
      final String particle) {
    assertEquals(id, coating.asString());
    assertEquals(source, coating.weakenedSource());
    assertEquals(sound, coating.wearOffSound().getId().getPath());
    assertEquals(
        particle,
        net.minecraft.registry.Registries.PARTICLE_TYPE
            .getId(coating.wearOffParticle().getType())
            .getPath());
    assertEquals("nightborn.coating." + id, coating.translationKey());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Coating.class)
  @DisplayName("each coating reads only its own settings and saves by name")
  void settingsAndCodec(final Coating coating) {
    final List<?> own =
        List.of(
            coating.damage, coating.hits, coating.seconds, coating.strength, coating.weakenSeconds);
    assertTrue(CoatingSettings.SETTINGS.containsAll(own));
    own.forEach(
        setting ->
            assertTrue(
                setting.toString().contains(coating == Coating.GARLIC ? "garlic" : "holyWater")));
    assertEquals(
        coating,
        Coating.CODEC
            .parse(
                JsonOps.INSTANCE, Coating.CODEC.encodeStart(JsonOps.INSTANCE, coating).getOrThrow())
            .getOrThrow());
  }
}
