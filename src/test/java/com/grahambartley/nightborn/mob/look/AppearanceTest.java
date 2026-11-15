package com.grahambartley.nightborn.mob.look;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PrimitiveIterator;
import java.util.stream.DoubleStream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AppearanceTest {

  private static final Map<Identifier, VampireLook> LOOKS =
      Map.of(
          id("anna"), new VampireLook(VampireRig.FEMALE, id("anna.png"), List.of(id("v")), 1),
          id("bram"), new VampireLook(VampireRig.MALE, id("bram.png"), List.of(id("g")), 1));
  private static final Map<Identifier, VampireOutfit> OUTFITS =
      Map.of(
          id("gown"), new VampireOutfit(VampireRig.FEMALE, id("v"), List.of(), 1),
          id("coat"), new VampireOutfit(VampireRig.MALE, id("g"), List.of(), 1),
          id("cape"), new VampireOutfit(VampireRig.MALE, id("v"), List.of(), 1));
  private static final Map<Identifier, VampireBuild> BUILDS =
      Map.of(
          id("broad"), new VampireBuild(1.14F, 1.5F, 1.85F, 1),
          id("slim"), new VampireBuild(0.88F, 1.7F, 2.0F, 1));

  @ParameterizedTest(name = "rolls {0} {1} {2} {3}: {4} in {5}, {6} build, {7} tall")
  @CsvSource({
    "0.1, 0.5, 0.1, 0.0, anna, gown, 1.14, 1.5",
    "0.9, 0.5, 0.9, 1.0, bram, coat, 0.88, 2.0",
    "0.6, 0.0, 0.2, 0.5, bram, coat, 1.14, 1.675"
  })
  @DisplayName("a look is picked, then an outfit of its rig and era, then a build and a height")
  void picks(
      final double lookRoll,
      final double outfitRoll,
      final double buildRoll,
      final double heightRoll,
      final String look,
      final String outfit,
      final float width,
      final float height) {
    final Appearance appearance =
        Appearance.pick(LOOKS, OUTFITS, BUILDS, rolls(lookRoll, outfitRoll, buildRoll, heightRoll))
            .orElseThrow();
    assertEquals(id(look), appearance.look());
    assertEquals(Optional.of(id(outfit)), appearance.outfit());
    assertEquals(width, appearance.width(), 1.0E-5F);
    assertEquals(height, appearance.height(), 1.0E-4F);
  }

  @Test
  @DisplayName("with no builds a vampire keeps the base size, and with no looks there is nothing")
  void fallbacks() {
    final Appearance plain =
        Appearance.pick(LOOKS, Map.of(), Map.of(), rolls(0.1, 0.1, 0.1, 0.1)).orElseThrow();
    assertEquals(Optional.empty(), plain.outfit());
    assertEquals(VampireSize.BASE_HEIGHT, plain.height());
    assertEquals(1.0F, plain.width());
    assertTrue(Appearance.pick(Map.of(), OUTFITS, BUILDS, rolls(0.1)).isEmpty());
  }

  private static java.util.function.DoubleSupplier rolls(final double... values) {
    final PrimitiveIterator.OfDouble it = DoubleStream.of(values).iterator();
    return () -> it.hasNext() ? it.nextDouble() : 0.0;
  }

  private static Identifier id(final String path) {
    return Identifier.of("nightborn", path);
  }
}
