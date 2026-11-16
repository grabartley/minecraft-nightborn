package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import software.bernie.geckolib.cache.object.GeoBone;

class BustShapeTest {

  private static final float TOLERANCE = 1.0E-5F;

  @ParameterizedTest(name = "{0} at {1}")
  @CsvSource({"bust, 0.7", "bust, 1.0", "bust, 1.3", "dyed_bust, 1.3", "points_bust, 0.7"})
  @DisplayName("a bust bone keeps its top edge on the chest and grows away from it")
  void scalesAboutTheTopEdge(final String name, final float size) {
    final MatrixStack stack = new MatrixStack();
    BustShape.size(stack, bone(name, 0, 21, -2), size);
    final Vector3f edge = transform(stack, 0, 21 / 16.0F, -2 / 16.0F);
    assertEquals(0, edge.x, TOLERANCE);
    assertEquals(21 / 16.0F, edge.y, TOLERANCE);
    assertEquals(-2 / 16.0F, edge.z, TOLERANCE);
    final Vector3f tip = transform(stack, 0, 18 / 16.0F, -3 / 16.0F);
    assertEquals((21 - 3 * size) / 16.0F, tip.y, TOLERANCE);
    assertEquals((-2 - size) / 16.0F, tip.z, TOLERANCE);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"body", "head", "dyed_bodice", "busk_point"})
  @DisplayName("every other bone is left alone")
  void leavesOtherBonesAlone(final String name) {
    final MatrixStack stack = new MatrixStack();
    BustShape.size(stack, bone(name, 0, 21, -2), 1.3F);
    final Vector3f point = transform(stack, 0.5F, 1.2F, -0.3F);
    assertEquals(0.5F, point.x, TOLERANCE);
    assertEquals(1.2F, point.y, TOLERANCE);
    assertEquals(-0.3F, point.z, TOLERANCE);
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({"VANILLA, true", "GECKO, false", "NONE, false"})
  @DisplayName("only a vanilla chestplate flattens the body under it")
  void flattensUnderAPlate(final ArmorDraw chest, final boolean flattened) {
    assertEquals(flattened, BustShape.underPlate(chest));
  }

  private static GeoBone bone(final String name, final float x, final float y, final float z) {
    final GeoBone bone = new GeoBone(null, name, false, null, null, null);
    bone.setPivotX(x);
    bone.setPivotY(y);
    bone.setPivotZ(z);
    return bone;
  }

  private static Vector3f transform(
      final MatrixStack stack, final float x, final float y, final float z) {
    return stack.peek().getPositionMatrix().transformPosition(new Vector3f(x, y, z));
  }
}
