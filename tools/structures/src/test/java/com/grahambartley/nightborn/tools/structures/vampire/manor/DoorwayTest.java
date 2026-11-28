package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Axis;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DoorwayTest {

  static Template wall() {
    final Template t = new Template("vampire/test", 9, 8, 9);
    t.fill(0, 0, 4, 8, 7, 5, "minecraft:stone");
    return t;
  }

  @ParameterizedTest(name = "a {0} wide doorway is {1} tall")
  @CsvSource({"1, 3", "2, 3", "3, 4"})
  void widerDoorwaysStandTaller(final int width, final int height) {
    final Doorway doorway = Doorway.arch(Axis.X, 2, 1 + width, 4, 1, Manor.TRIM);
    assertEquals(width, doorway.width());
    assertEquals(height, doorway.height());
  }

  @ParameterizedTest(name = "facing {0}, the leaf at {1} hinges {2}")
  @CsvSource({
    "north, 2, left",
    "north, 3, right",
    "south, 2, right",
    "south, 3, left",
    "east, 2, left",
    "east, 3, right",
    "west, 2, right",
    "west, 3, left"
  })
  void leavesHingeOnTheirOuterJambs(final String facing, final int at, final String hinge) {
    final Axis along = facing.equals("north") || facing.equals("south") ? Axis.X : Axis.Z;
    assertEquals(hinge, Doorway.doors(along, 2, 3, 4, 4, 1, facing).hinge(at));
  }

  @ParameterizedTest(name = "a {0} wide door through a {1} deep wall opens clear")
  @CsvSource({"2, 1", "2, 2", "3, 2"})
  void cutsThroughTheWholeWall(final int width, final int depth) {
    final Template t = wall();
    final Doorway doorway = Doorway.doors(Axis.X, 2, 1 + width, 4, 3 + depth, 1, "north");
    doorway.cut(t);
    for (int x = 2; x <= 1 + width; x++) {
      for (int z = 4; z <= 3 + depth; z++) {
        final String state = t.get(x, 1, z);
        final boolean leaf = z == 4 && (x == 2 || x == 1 + width);
        assertTrue(leaf ? state.contains("dark_oak_door") : BlockState.AIR.equals(state), state);
      }
    }
    assertEquals("minecraft:stone", t.get(2, 1 + doorway.height(), 4));
  }

  @ParameterizedTest(name = "a three wide arch gets shoulders {0} at {1}")
  @CsvSource({"west, 2", "east, 4"})
  void wideArchesHaveShoulders(final String facing, final int x) {
    final Template t = wall();
    Doorway.arch(Axis.X, 2, 4, 4, 1, Manor.TRIM).cut(t);
    final String shoulder = t.get(x, 4, 4);
    assertTrue(shoulder.contains("facing=" + facing) && shoulder.contains("half=top"), shoulder);
    assertEquals(BlockState.AIR, t.get(3, 4, 4));
  }
}
