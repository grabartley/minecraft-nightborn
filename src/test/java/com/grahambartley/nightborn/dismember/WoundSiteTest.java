package com.grahambartley.nightborn.dismember;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WoundSiteTest {

  private static final BodyZones ZONES = new BodyZones(0.25, 0.375, 0.5, Map.of(BodyPart.HEAD, 1));
  private static final double WIDTH = 0.6;
  private static final double HEIGHT = 1.8;

  @ParameterizedTest(name = "{0} facing {1}: ({2}, {3}, {4})")
  @CsvSource({
    "HEAD, 0, 0, 1.35, 0",
    "RIGHT_ARM, 0, -0.225, 1.2375, 0",
    "LEFT_ARM, 0, 0.225, 1.2375, 0",
    "RIGHT_LEG, 0, -0.15, 0.675, 0",
    "LEFT_LEG, 0, 0.15, 0.675, 0",
    "RIGHT_ARM, 90, 0, 1.2375, -0.225",
    "LEFT_LEG, 180, -0.15, 0.675, 0"
  })
  @DisplayName("a cut sits at the neck, the shoulder or the hip of the body, turned with it")
  void offset(
      final BodyPart part, final float yaw, final double x, final double y, final double z) {
    final Vec3d at = WoundSite.offset(ZONES, part, WIDTH, HEIGHT, yaw);
    assertEquals(x, at.x, 1.0e-6);
    assertEquals(y, at.y, 1.0e-6);
    assertEquals(z, at.z, 1.0e-6);
  }

  @ParameterizedTest(name = "{0} facing {1}")
  @CsvSource({"HEAD, 0", "RIGHT_ARM, 0", "LEFT_ARM, 37", "RIGHT_LEG, 120", "LEFT_LEG, 300"})
  @DisplayName(
      "hit location reads each cut site, just past its joint line, as the part it belongs to")
  void readsBackAsThePart(final BodyPart part, final float yaw) {
    final Box body = new Box(-WIDTH / 2, 0.0, -WIDTH / 2, WIDTH / 2, HEIGHT, WIDTH / 2);
    final Vec3d site = WoundSite.offset(ZONES, part, WIDTH, HEIGHT, yaw);
    final Vec3d nudged =
        switch (part) {
          case HEAD -> site.add(0.0, 0.01, 0.0);
          case RIGHT_LEG, LEFT_LEG -> site.add(0.0, -0.01, 0.0);
          default -> site;
        };
    assertEquals(
        part,
        ZONES
            .at(HitLocation.height(body, nudged), HitLocation.side(body, nudged, yaw))
            .orElseThrow());
  }

  @ParameterizedTest(name = "{0} facing {1}: ({2}, {3}, {4})")
  @CsvSource({
    "HEAD, 45, 0, 1, 0",
    "RIGHT_ARM, 0, -1, 0.25, 0",
    "LEFT_ARM, 0, 1, 0.25, 0",
    "RIGHT_LEG, 0, -0.5, -0.5, 0",
    "LEFT_LEG, 90, 0, -0.5, 0.5"
  })
  @DisplayName("blood leaves a cut away from the body: up from the neck, out from a limb")
  void outward(
      final BodyPart part, final float yaw, final double x, final double y, final double z) {
    final Vec3d out = WoundSite.outward(part, yaw);
    assertEquals(x, out.x, 1.0e-6);
    assertEquals(y, out.y, 1.0e-6);
    assertEquals(z, out.z, 1.0e-6);
  }
}
