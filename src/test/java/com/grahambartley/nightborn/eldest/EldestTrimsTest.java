package com.grahambartley.nightborn.eldest;

import com.grahambartley.nightborn.testing.ShippedTrims;
import java.io.IOException;
import java.util.stream.Stream;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.registry.RegistryKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EldestTrimsTest {

  static Stream<Arguments> trims() {
    return Stream.of(
        Arguments.of("fang", EldestTrims.FANG), Arguments.of("sigil", EldestTrims.SIGIL));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("trims")
  @DisplayName("each trim has a pattern that names its own template, a title and a smithing recipe")
  void shipped(final String name, final RegistryKey<ArmorTrimPattern> key) throws IOException {
    ShippedTrims.assertShipped(name, key);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("trims")
  @DisplayName("each trim draws its own pattern, shipped for armour and leggings and in the atlas")
  void drawsItsOwnPattern(final String name, final RegistryKey<ArmorTrimPattern> key)
      throws IOException {
    ShippedTrims.assertDrawsItsOwnPattern(name);
  }
}
