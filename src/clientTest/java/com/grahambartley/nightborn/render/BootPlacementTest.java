package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import net.minecraft.client.model.ModelPart;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import software.bernie.geckolib.cache.object.GeoBone;

class BootPlacementTest {

  private static final float TOLERANCE = 1.0E-5F;

  @ParameterizedTest(name = "{0} leg pivot {1},{2},{3} turned {4},{5},{6}")
  @CsvSource({
    "RIGHT, -1.9, 12, 0, 0, 0, 0, 0.1, 0, 0",
    "LEFT, 1.9, 12, 0, 0, 0, 0, -0.1, 0, 0",
    "RIGHT, 3, -14, 0, 0, 0, 0, 5, 26, 0",
    "LEFT, -3, -14, 0, 0, 0, 0, -5, 26, 0",
    "RIGHT, 3, -14, 0.5, 0.4, -0.2, 0.3, 5, 26, 0.5",
    "LEFT, -3, -13, -1, -0.6, 0.1, -0.25, -5, 25, -1"
  })
  @DisplayName("a boot model with no leg bone sits and turns where the vanilla leg does")
  void placesBootsFromTheLeg(
      final BootPlacement side,
      final float pivotX,
      final float pivotY,
      final float pivotZ,
      final float pitch,
      final float yaw,
      final float roll,
      final float x,
      final float y,
      final float z) {
    final GeoBone boot = bone("armorBoot");
    side.place(null, boot, leg(pivotX, pivotY, pivotZ, pitch, yaw, roll));
    assertEquals(x, boot.getPosX(), TOLERANCE);
    assertEquals(y, boot.getPosY(), TOLERANCE);
    assertEquals(z, boot.getPosZ(), TOLERANCE);
    assertEquals(-pitch, boot.getRotX(), TOLERANCE);
    assertEquals(-yaw, boot.getRotY(), TOLERANCE);
    assertEquals(roll, boot.getRotZ(), TOLERANCE);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(BootPlacement.class)
  @DisplayName("boots on a model with a leg bone are left to the leg's own placement")
  void leavesBootsWithALegBoneAlone(final BootPlacement side) {
    final GeoBone boot = bone("armorBoot");
    side.place(bone("armorLeg"), boot, leg(3, -14, 0.5F, 0.4F, -0.2F, 0.3F));
    assertEquals(0, boot.getPosX());
    assertEquals(0, boot.getPosY());
    assertEquals(0, boot.getPosZ());
    assertEquals(0, boot.getRotX());
    assertEquals(0, boot.getRotY());
    assertEquals(0, boot.getRotZ());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(BootPlacement.class)
  @DisplayName("a model with no boots is a no-op")
  void ignoresModelsWithoutBoots(final BootPlacement side) {
    assertDoesNotThrow(() -> side.place(null, null, leg(3, -14, 0, 0, 0, 0)));
  }

  private static GeoBone bone(final String name) {
    return new GeoBone(null, name, false, null, null, null);
  }

  private static ModelPart leg(
      final float pivotX,
      final float pivotY,
      final float pivotZ,
      final float pitch,
      final float yaw,
      final float roll) {
    final ModelPart part = new ModelPart(List.of(), Map.of());
    part.setPivot(pivotX, pivotY, pivotZ);
    part.setAngles(pitch, yaw, roll);
    return part;
  }
}
