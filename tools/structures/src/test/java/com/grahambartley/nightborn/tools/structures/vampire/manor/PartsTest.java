package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
