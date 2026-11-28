package com.grahambartley.nightborn.client.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DecalShapesTest {

  @ParameterizedTest(name = "{0} shapes, roll {1}: rim {2}, body {3}, last {4}")
  @CsvSource({
    "4, 0, 0, 4, 7",
    "4, 3, 3, 7, 7",
    "4, 6, 2, 6, 7",
    "4, -1, 3, 7, 7",
    "3, 5, 2, 5, 5",
    "1, 9, 0, 1, 1"
  })
  @DisplayName("each rim sprite pairs with the body sprite of the same shape")
  void pairs(final int count, final int roll, final int rim, final int body, final int last) {
    final DecalShapes shapes = new DecalShapes(count);
    assertEquals(rim, shapes.rim(roll));
    assertEquals(body, shapes.body(roll));
    assertEquals(last, shapes.lastSprite());
  }

  @Test
  @DisplayName("a decal must have a shape")
  void needsAShape() {
    assertThrows(IllegalArgumentException.class, () -> new DecalShapes(0));
  }
}
