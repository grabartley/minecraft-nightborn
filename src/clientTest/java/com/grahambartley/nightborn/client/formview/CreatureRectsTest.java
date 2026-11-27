package com.grahambartley.nightborn.client.formview;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class CreatureRectsTest {

  private static final Matrix4f LOOKING_NORTH =
      new Matrix4f().perspective((float) Math.toRadians(90.0), 1.0F, 0.05F, 512.0F);

  @Test
  @DisplayName("a creature straight ahead covers the middle of the screen")
  void aheadIsCentred() {
    final float[] rect =
        CreatureRects.screenRect(new Box(-0.5, -0.5, -10.5, 0.5, 0.5, -9.5), LOOKING_NORTH);
    assertTrue(rect[0] < 0.5F && rect[2] > 0.5F, "spans the middle across");
    assertTrue(rect[1] < 0.5F && rect[3] > 0.5F, "spans the middle up and down");
    assertEquals(rect[0], 1.0F - rect[2], 1.0E-5F);
  }

  @ParameterizedTest(name = "x {0} lands {1}")
  @CsvSource({"-5, left", "5, right"})
  @DisplayName("a creature to one side lands on that side of the screen")
  void sideIsSide(final double x, final String side) {
    final float[] rect =
        CreatureRects.screenRect(new Box(x - 0.5, -0.5, -10.5, x + 0.5, 0.5, -9.5), LOOKING_NORTH);
    if ("left".equals(side)) {
      assertTrue(rect[2] < 0.5F);
    } else {
      assertTrue(rect[0] > 0.5F);
    }
  }

  @Test
  @DisplayName("a creature reaching behind the eye covers the whole screen so it is never missed")
  void behindCoversAll() {
    assertArrayEquals(
        new float[] {0.0F, 0.0F, 1.0F, 1.0F},
        CreatureRects.screenRect(new Box(-0.5, -1.0, -0.5, 0.5, 1.0, 0.5), LOOKING_NORTH));
  }

  @ParameterizedTest(name = "box from z {0} to {1}: nearest {2}")
  @CsvSource({"-11, -9, 9.0", "9, 11, 9.0", "-1, 1, 0.0"})
  @DisplayName("the nearest distance is to the closest point of the box")
  void nearest(final double from, final double to, final double distance) {
    assertEquals(distance, CreatureRects.nearest(new Box(-0.5, -0.5, from, 0.5, 0.5, to)), 1.0E-6);
  }

  @Test
  @DisplayName("the farthest distance is to the farthest corner")
  void farthest() {
    assertEquals(
        Math.sqrt(1 + 4 + 9), CreatureRects.farthest(new Box(-1, -2, -3, 1, 2, 3)), 1.0E-6);
  }

  @ParameterizedTest(name = "{0} creatures")
  @ValueSource(ints = {0, 1, 16, 17, 40})
  @DisplayName("no more than the fixed number of creatures are packed, and empty slots stay empty")
  void packsUpToCapacity(final int count) {
    final CreatureRects.Packed packed =
        CreatureRects.pack(
            Collections.nCopies(count, new Box(-0.5, -0.5, -10.5, 0.5, 0.5, -9.5)), LOOKING_NORTH);
    assertEquals(CreatureRects.CAPACITY * 4, packed.rects().length);
    assertEquals(CreatureRects.CAPACITY * 2, packed.depths().length);
    final int used = Math.min(count, CreatureRects.CAPACITY);
    for (int i = 0; i < CreatureRects.CAPACITY; i++) {
      assertEquals(i < used, packed.depths()[i * 2 + 1] > 0.0F, "slot " + i);
    }
  }

  @Test
  @DisplayName("an empty list packs no creatures")
  void emptyPacksNothing() {
    final CreatureRects.Packed packed = CreatureRects.pack(List.of(), LOOKING_NORTH);
    for (final float depth : packed.depths()) {
      assertEquals(0.0F, depth);
    }
  }
}
