package com.grahambartley.nightborn.client.sun;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ParasolModelTest {

  private static final ParasolModel MODEL = new ParasolModel();

  static Stream<Arguments> resources() {
    return Stream.of(
        Arguments.of(
            "geometry",
            (Function<ParasolModel, Identifier>) model -> model.getModelResource(null),
            "nightborn:geo/item/parasol.geo.json"),
        Arguments.of(
            "texture",
            (Function<ParasolModel, Identifier>) model -> model.getTextureResource(null),
            "nightborn:textures/entity/parasol/fresh.png"),
        Arguments.of(
            "animations",
            (Function<ParasolModel, Identifier>) model -> model.getAnimationResource(null),
            "nightborn:animations/item/parasol.animation.json"));
  }

  @ParameterizedTest(name = "{0} is {2}")
  @MethodSource("resources")
  @DisplayName("the parasol draws from its exported art")
  void readsItsArt(
      final String what, final Function<ParasolModel, Identifier> read, final String expected) {
    assertEquals(expected, read.apply(MODEL).toString());
  }
}
