package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ManorFrontTest {

  static Template front;

  @BeforeAll
  static void build() {
    front = new Template("vampire/test", Manor.SIZE_X, Manor.SIZE_Y, Manor.SIZE_Z);
    ManorShell.build(front);
    ManorFront.build(front);
  }

  @ParameterizedTest(name = "{0},{1} in the bay: {2}")
  @CsvSource({
    "15, 28, true",
    "15, 29, false",
    "16, 29, true",
    "19, 29, false",
    "17, 27, true",
    "20, 28, false"
  })
  void theBayIsCanted(final int x, final int z, final boolean in) {
    assertEquals(in, ManorFront.inBay(x, z));
  }

  @ParameterizedTest(name = "the bay is glazed in stained glass at y {0}")
  @ValueSource(ints = {5, 6, 7})
  void theBayIsGlazed(final int y) {
    assertEquals(Manor.STAINED, front.get(17, y, ManorFront.BAY_S).replace("minecraft:", ""));
  }

  @ParameterizedTest(name = "step {0} rises to y {1}")
  @CsvSource({"29, 1", "28, 2", "27, 3"})
  void stepsClimbToThePorch(final int z, final int y) {
    assertTrue(front.get(Manor.MID_X, y, z).contains("facing=north"), front.get(Manor.MID_X, y, z));
    final String above = front.get(Manor.MID_X, y + 1, z);
    assertTrue(above == null || BlockState.AIR.equals(above), above);
  }

  @ParameterizedTest(name = "the bedroom wall above the bay at x {0} is {1}")
  @CsvSource({"16, glass_pane", "17, bricks", "18, glass_pane"})
  void theBayRoofLeavesTheWallAboveWhole(final int x, final String block) {
    assertTrue(
        front.get(x, Manor.F1 + 1, Manor.WING_S).contains(block),
        front.get(x, Manor.F1 + 1, Manor.WING_S));
  }

  @ParameterizedTest(name = "the porch arch is open at y {0}")
  @ValueSource(ints = {4, 5, 6})
  void thePorchArchIsOpen(final int y) {
    assertEquals(BlockState.AIR, front.get(Manor.MID_X, y, ManorFront.PORCH_S));
  }

  @ParameterizedTest(name = "a gargoyle guards x {0}")
  @ValueSource(ints = {25, 29})
  void gargoylesGuardTheSteps(final int x) {
    assertTrue(front.get(x, 3, ManorFront.STEPS_END).startsWith(Manor.GARGOYLE + "[facing=south"));
  }

  @ParameterizedTest(name = "the great window rises to y {1} at x {0}")
  @CsvSource({"26, 12", "27, 14", "28, 12"})
  void theGreatWindowIsATripleLancet(final int x, final int top) {
    assertTrue(front.get(x, top, Manor.MAIN_S).contains("stained_glass_pane"));
    final String above = front.get(x, top + 1, Manor.MAIN_S);
    assertTrue(above == null || !above.contains("glass"), above);
  }
}
