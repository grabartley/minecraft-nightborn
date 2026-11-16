package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.mob.look.Figure;
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
  @CsvSource({
    "bust, true",
    "dyed_bust, true",
    "points_bust, true",
    "cravat_bust, true",
    "busk_point, false",
    "body, false",
    "armor_body, false"
  })
  @DisplayName("a bone is part of the bust when its name ends in bust")
  void knowsTheBustBones(final String name, final boolean bust) {
    assertEquals(bust, BustShape.isBust(bone(name, 0, 0, 0)));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({"figure, true", "player, false", "nobody, false"})
  @DisplayName("clothing shapes a bust only on a wearer whose body has one")
  void shapesClothingOnlyOnAFigure(final String wearer, final boolean worn) {
    final Object who =
        switch (wearer) {
          case "figure" -> new Figure() {};
          case "player" -> new Object();
          default -> null;
        };
    assertEquals(worn, BustShape.worn(who));
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
