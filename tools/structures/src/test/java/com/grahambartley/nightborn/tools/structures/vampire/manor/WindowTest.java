package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.RouteRoom;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WindowTest {

  @ParameterizedTest(name = "a window facing {0} has its sill and hood at {1},{2}")
  @CsvSource({"south, 4, 5", "north, 4, 3", "east, 5, 4", "west, 3, 4"})
  void dressesTheOutsideFace(final String outward, final int ox, final int oz) {
    final Template t = new Template("vampire/test", 9, 16, 9);
    final Window window = new Window(4, 4, outward, 5, 3, Manor.PANE);
    window.cut(t);
    for (int y = 5; y < 8; y++) {
      assertEquals("minecraft:glass_pane", t.get(4, y, 4));
    }
    assertTrue(t.get(ox, 8, oz).contains("half=top"), t.get(ox, 8, oz));
    assertTrue(t.get(ox, 4, oz).contains("type=top"), t.get(ox, 4, oz));
  }

  @ParameterizedTest(name = "{0} windows sit at y {1}")
  @CsvSource({"ground, 5", "upper, 10"})
  void floorsSetTheSill(final String floor, final int sill) {
    final Window window =
        "ground".equals(floor) ? Window.ground(1, 1, "south") : Window.upper(1, 1, "south");
    assertEquals(sill, window.sill());
    assertEquals(sill + Window.TALL, window.head());
  }

  @ParameterizedTest(name = "a door facing {0} has two halves under an open fanlight")
  @CsvSource({"north", "east"})
  void doorsAreTwoHighUnderAFanlight(final String facing) {
    final Template t = new Template("vampire/test", 3, 6, 3);
    t.fill(0, 0, 0, 2, 5, 2, "bricks");
    Window.door(t, 1, 1, 1, facing);
    assertTrue(t.get(1, 1, 1).contains("half=lower"));
    assertTrue(t.get(1, 2, 1).contains("half=upper"));
    for (int y = 3; y <= RouteRoom.HOUSEHOLD; y++) {
      assertEquals(BlockState.AIR, t.get(1, y, 1), "fanlight at " + y);
    }
    assertEquals("minecraft:bricks", t.get(1, RouteRoom.HOUSEHOLD + 1, 1));
  }
}
