package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.vampire.Axis;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ManorWindowsTest {

  static Stream<Window> windows() {
    return ManorWindows.WINDOWS.stream();
  }

  @ParameterizedTest(name = "{0} sits in an outer wall away from its corners")
  @MethodSource("windows")
  void everyWindowIsInAnOuterWall(final Window window) {
    assertTrue(ManorShell.HOUSE.onWall(window.x(), window.z()));
    final Pos in = window.reveal();
    assertTrue(ManorShell.LINING.onWall(in.x(), in.z()), "reveal " + in);
  }

  @ParameterizedTest(name = "{0} stays clear of doorways and chimneys")
  @MethodSource("windows")
  void everyWindowStaysClearOfDoorsAndFires(final Window window) {
    for (final Chimney chimney : Chimney.ALL) {
      final boolean behindStack =
          window.x() + 1 >= chimney.x0()
              && window.x() - 1 <= chimney.x1()
              && window.z() >= chimney.z0()
              && window.z() <= chimney.z1();
      assertTrue(!behindStack, chimney.toString());
    }
    for (final Doorway doorway : ManorShell.DOORWAYS) {
      final int a = doorway.along() == Axis.X ? window.x() : window.z();
      final int c = doorway.along() == Axis.X ? window.z() : window.x();
      final boolean sameRows =
          window.sill() < doorway.floor() + doorway.height() && window.head() > doorway.floor();
      assertTrue(
          !(sameRows && a >= doorway.from() - 1 && a <= doorway.to() + 1 && c == doorway.line()),
          doorway.toString());
    }
  }

  @ParameterizedTest(name = "{0} is curtained in velvet and sits under the eaves")
  @MethodSource("windows")
  void everyWindowHasADrape(final Window window) {
    assertTrue(window.drape().endsWith("_velvet_drape"));
    assertTrue(window.sill() >= Manor.F0 + 2 && window.head() <= Manor.TOP - 1);
  }
}
