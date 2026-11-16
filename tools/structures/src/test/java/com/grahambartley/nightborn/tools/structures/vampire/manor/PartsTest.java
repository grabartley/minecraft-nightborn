package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PartsTest {

  @ParameterizedTest(name = "a {1} {0} stair")
  @CsvSource({"north, bottom", "east, top"})
  void stairsKeepFacingAndHalf(final String facing, final String half) {
    final String stair = Parts.stair("minecraft:oak_stairs", facing, half);
    assertTrue(stair.contains("facing=" + facing) && stair.contains("half=" + half), stair);
  }

  @ParameterizedTest(name = "a hood mould facing {0} is upside down")
  @ValueSource(strings = {"south", "west"})
  void hoodsAreUpsideDown(final String facing) {
    assertTrue(Parts.hood(Manor.TRIM, facing).contains("half=top"));
  }

  @ParameterizedTest(name = "a vine on the {0} side holds only that side")
  @ValueSource(strings = {"north", "south", "east", "west", "up"})
  void vinesHoldOneSide(final String side) {
    final String vine = Parts.vine(side);
    for (final String other : new String[] {"north", "south", "east", "west", "up"}) {
      assertTrue(vine.contains(other + "=" + other.equals(side)), vine);
    }
  }

  @ParameterizedTest(name = "a door's {0} half starts shut")
  @ValueSource(strings = {"lower", "upper"})
  void doorsStartShut(final String half) {
    final String door = Parts.door(Manor.DOOR, "north", half, "left");
    assertTrue(door.contains("half=" + half) && door.contains("open=false"), door);
  }

  @ParameterizedTest(name = "a flight rising {0} from {1},{2} steps down one block a tread")
  @CsvSource({"north, 3, 2, 3, 4", "south, 3, 4, 3, 2", "east, 4, 3, 2, 3", "west, 2, 3, 4, 3"})
  void flightsDropOneBlockATread(
      final String up, final int x, final int z, final int lowX, final int lowZ) {
    final Template t = new Template("vampire/test", 7, 5, 7);
    Parts.flight(t, "minecraft:oak_stairs", "minecraft:stone", x, z, up, 3, 3, 0);
    assertTrue(t.get(x, 3, z).contains("facing=" + up));
    assertTrue(t.get(lowX, 1, lowZ).contains("oak_stairs"), t.get(lowX, 1, lowZ));
    assertTrue("minecraft:stone".equals(t.get(lowX, 0, lowZ)));
    assertTrue("minecraft:stone".equals(t.get(x, 2, z)));
  }

  @ParameterizedTest(name = "a fence joined n {0} s {1} e {2} w {3}")
  @CsvSource({"true, true, false, false", "true, false, true, false", "false, false, true, true"})
  void fencesJoinTheirNamedSides(
      final boolean north, final boolean south, final boolean east, final boolean west) {
    final String fence = Parts.fence(north, south, east, west);
    assertTrue(
        fence.contains("north=" + north)
            && fence.contains("south=" + south)
            && fence.contains("east=" + east)
            && fence.contains("west=" + west),
        fence);
  }

  @ParameterizedTest(name = "a hanging {0} lantern")
  @ValueSource(booleans = {true, false})
  void lanternsHangOrStand(final boolean hanging) {
    assertTrue(Parts.lantern(hanging).contains("hanging=" + hanging));
  }

  @ParameterizedTest(name = "a campfire facing {0} burns")
  @ValueSource(strings = {"east", "west"})
  void campfiresBurn(final String facing) {
    final String fire = Parts.campfire(facing);
    assertTrue(fire.contains("lit=true") && fire.contains("signal_fire=false"), fire);
  }
}
