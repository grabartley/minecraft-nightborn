package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ChimneyTest {

  static Stream<Arguments> fires() {
    return Chimney.ALL.stream()
        .flatMap(chimney -> chimney.fires().stream().map(fire -> Arguments.of(chimney, fire)));
  }

  static Template built(final Chimney chimney) {
    final Template t = new Template("vampire/test", Manor.SIZE_X, Manor.SIZE_Y, Manor.SIZE_Z);
    ManorShell.build(t);
    chimney.build(t);
    return t;
  }

  @ParameterizedTest(name = "{1} burns with an open flue")
  @MethodSource("fires")
  void everyFireDrawsToTheSky(final Chimney chimney, final Chimney.Fire fire) {
    final Template t = built(chimney);
    final Pos box = fire.box();
    assertTrue(t.get(box.x(), box.y(), box.z()).startsWith("minecraft:campfire"));
    for (int y = box.y() + 1; y < t.sizeY(); y++) {
      final String above = t.get(box.x(), y, box.z());
      assertTrue(above == null || BlockState.AIR.equals(above), "flue blocked at y " + y);
    }
  }

  @ParameterizedTest(name = "{1} opens into its room")
  @MethodSource("fires")
  void everyFireOpensIntoItsRoom(final Chimney chimney, final Chimney.Fire fire) {
    final Template t = built(chimney);
    assertEquals(BlockState.AIR, t.get(fire.x(), fire.floor() + 1, fire.z()));
    assertEquals(BlockState.AIR, t.get(fire.x(), fire.floor() + 2, fire.z()));
    final Pos hearth = fire.hearth();
    assertEquals("minecraft:polished_blackstone", t.get(hearth.x(), hearth.y(), hearth.z()));
    assertTrue(t.get(hearth.x(), fire.floor() + 3, hearth.z()).contains("half=top"));
  }

  @ParameterizedTest(name = "{0} narrows above the eaves")
  @MethodSource("fires")
  void stacksNarrowAboveTheEaves(final Chimney chimney, final Chimney.Fire fire) {
    final Template t = built(chimney);
    assertTrue(chimney.shaft(fire.box().x(), fire.box().z()));
    for (int x = chimney.x0(); x <= chimney.x1(); x++) {
      for (int z = chimney.z0(); z <= chimney.z1(); z++) {
        final boolean shaft = chimney.shaft(x, z);
        assertEquals(shaft, t.get(x, Manor.TOP + 2, z) != null, x + "," + z);
      }
    }
  }

  @ParameterizedTest(name = "{1} burns deep inside the two thick wall")
  @MethodSource("fires")
  void everyFireboxSitsInsideTheWall(final Chimney chimney, final Chimney.Fire fire) {
    final Template t = built(chimney);
    final Pos firebox = fire.firebox();
    assertTrue(ManorShell.HOUSE.onWall(firebox.x(), firebox.z()));
    assertTrue(ManorShell.LINING.onWall(fire.x(), fire.z()));
    assertEquals(BlockState.AIR, t.get(firebox.x(), firebox.y(), firebox.z()));
    assertTrue(chimney.shaft(fire.box().x(), fire.box().z()));
  }

  @ParameterizedTest(name = "{0} stands clear of the roof slope beside it")
  @MethodSource("fires")
  void stacksRiseAboveTheRoof(final Chimney chimney, final Chimney.Fire fire) {
    for (int z = chimney.z0(); z <= chimney.z1(); z++) {
      assertTrue(chimney.top() >= Manor.TOP + ManorRoofs.rise(z) + Chimney.ABOVE_ROOF);
    }
  }
}
