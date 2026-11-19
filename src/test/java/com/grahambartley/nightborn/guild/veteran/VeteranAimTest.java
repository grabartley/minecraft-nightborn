package com.grahambartley.nightborn.guild.veteran;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VeteranAimTest {

  @ParameterizedTest(name = "{0} blocks every {1}: {2} marks")
  @CsvSource({"9.0, 0.75, 12", "3.0, 1.0, 3", "0.0, 0.75, 0", "5.0, 0.0, 0"})
  void marksTheLineOnTheFloorUpToTheTarget(
      final double length, final double step, final int marks) {
    final List<Vec3d> line = VeteranAim.line(Vec3d.ZERO, new Vec3d(length, 3.0, 0.0), step);
    assertEquals(marks, line.size());
    line.forEach(point -> assertEquals(0.0, point.y, 1.0E-9));
  }

  @ParameterizedTest(name = "{0} spots {1} apart")
  @CsvSource({"3, 2.0", "1, 2.0", "4, 1.5"})
  void spreadsTheArcAcrossTheThrowCentredOnTheTarget(final int count, final double spacing) {
    final Vec3d target = new Vec3d(0.0, 64.0, 10.0);
    final List<Vec3d> spots = VeteranAim.arc(Vec3d.ZERO, target, count, spacing);
    assertEquals(count, spots.size());
    final Vec3d centre = spots.stream().reduce(Vec3d.ZERO, Vec3d::add).multiply(1.0 / count);
    assertEquals(target.x, centre.x, 1.0E-9);
    assertEquals(target.z, centre.z, 1.0E-9);
    spots.forEach(spot -> assertEquals(target.z, spot.z, 1.0E-9));
    if (count > 1) {
      assertEquals(spacing, Math.abs(spots.get(1).x - spots.get(0).x), 1.0E-9);
    }
  }

  @ParameterizedTest(name = "target {0} away, gap {1}: lands {2} away")
  @CsvSource({"10.0, 1.5, 1.5", "1.0, 1.5, 1.0", "0.0, 1.5, 0.0"})
  void dragsTheTargetToJustInFrontOfTheVeteran(
      final double distance, final double gap, final double landed) {
    final Vec3d spot = VeteranAim.dragSpot(Vec3d.ZERO, new Vec3d(0.0, 0.0, distance), gap);
    assertEquals(landed, spot.horizontalLength(), 1.0E-9);
  }
}
