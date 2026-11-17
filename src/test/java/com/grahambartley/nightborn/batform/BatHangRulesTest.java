package com.grahambartley.nightborn.batform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BatHangRulesTest {

  @ParameterizedTest(name = "underside {0}, hanging {1}, head {2}, ceiling {3}, aside {4} -> {5}")
  @CsvSource({
    "true, false, 62.9, 64, 0.2, true",
    "true, false, 64.0, 64, 0.0, true",
    "true, false, 60.5, 64, 1.5, true",
    "false, false, 62.9, 64, 0.2, false",
    "true, true, 62.9, 64, 0.2, false",
    "true, false, 64.5, 64, 0.2, false",
    "true, false, 60.4, 64, 0.2, false",
    "true, false, 62.9, 64, 1.6, false"
  })
  @DisplayName("a bat hangs from the underside of a block it is beneath and close to")
  void reaches(
      final boolean underside,
      final boolean hanging,
      final double head,
      final double ceiling,
      final double aside,
      final boolean expected) {
    assertEquals(expected, BatHangRules.reaches(underside, hanging, head, ceiling, aside));
  }

  @ParameterizedTest(name = "anchor ({0}, {1}, {2})")
  @CsvSource({"0, 64, 0", "-3, 10, 7", "12, -20, -5"})
  @DisplayName("the bat hangs centred under its block, a block below its underside")
  void spot(final int x, final int y, final int z) {
    assertEquals(new Vec3d(x + 0.5, y - 1.0, z + 0.5), BatHangRules.spot(new BlockPos(x, y, z)));
  }

  @ParameterizedTest(name = "standing height {0}")
  @CsvSource({"1.8", "1.95", "0.9"})
  @DisplayName("turning back while hanging stands the vampire with its head under the block")
  void standingFeet(final double height) {
    assertEquals(
        new Vec3d(4.5, 64.0 - height, -1.5),
        BatHangRules.standingFeet(new BlockPos(4, 64, -2), height));
  }

  @ParameterizedTest(name = "{0} from the spot -> slips {1}")
  @CsvSource({"0.0, false", "0.25, false", "0.3, true", "4.0, true"})
  @DisplayName("a bat stops hanging once it has moved off its spot")
  void slips(final double distance, final boolean expected) {
    assertEquals(expected, BatHangRules.slips(distance));
  }

  @ParameterizedTest(name = "hanging {0}, pressing {1}, was armed {2} -> {3}")
  @CsvSource({
    "true, false, false, true",
    "true, true, false, false",
    "true, true, true, true",
    "true, false, true, true",
    "false, false, true, false",
    "false, true, true, false"
  })
  @DisplayName("a key still held from the flight up does not count until it is let go once")
  void armed(
      final boolean hanging, final boolean pressing, final boolean wasArmed, final boolean armed) {
    assertEquals(armed, BatHangRules.armed(hanging, pressing, wasArmed));
  }

  @ParameterizedTest(name = "armed {0}, pressing {1} -> {2}")
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("a fresh press of jump or forward lets a hanging bat go")
  void letsGo(final boolean armed, final boolean pressing, final boolean expected) {
    assertEquals(expected, BatHangRules.letsGo(armed, pressing));
  }

  @ParameterizedTest(name = "body at ({0}, {1}, {2})")
  @CsvSource({"0.5, 60.0, 0.5", "1.9, 61.0, 0.5", "0.5, 62.0, -0.9"})
  @DisplayName(
      "the way up to the hang spot covers the bat where it is, where it will hang and between")
  void sweep(final double x, final double y, final double z) {
    final Box body = new Box(x - 0.3, y, z - 0.3, x + 0.3, y + 0.9, z + 0.3);
    final Box hung = new Box(0.2, 63.0, 0.2, 0.8, 63.9, 0.8);
    final Box swept = BatHangRules.sweep(body, hung);
    assertEquals(Math.min(body.minX, hung.minX), swept.minX);
    assertEquals(Math.max(body.maxX, hung.maxX), swept.maxX);
    assertEquals(body.minY, swept.minY);
    assertEquals(hung.maxY, swept.maxY);
    assertEquals(Math.min(body.minZ, hung.minZ), swept.minZ);
    assertEquals(Math.max(body.maxZ, hung.maxZ), swept.maxZ);
  }
}
