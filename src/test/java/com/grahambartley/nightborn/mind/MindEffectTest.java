package com.grahambartley.nightborn.mind;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.entry.RegistryEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class MindEffectTest {

  static Stream<Arguments> effects() {
    return Stream.of(
        Arguments.of(MindEffect.MESMERISED, "mesmerised", StatusEffectCategory.HARMFUL),
        Arguments.of(MindEffect.CHARMED, "charmed", StatusEffectCategory.NEUTRAL));
  }

  @ParameterizedTest(name = "{1}")
  @MethodSource("effects")
  @DisplayName("mesmerised and charmed are status effects shown with their own names")
  void registered(
      final RegistryEntry<StatusEffect> effect,
      final String path,
      final StatusEffectCategory category) {
    assertEquals("nightborn:" + path, effect.getIdAsString());
    assertEquals("effect.nightborn." + path, effect.value().getTranslationKey());
    assertEquals(category, effect.value().getCategory());
  }
}
