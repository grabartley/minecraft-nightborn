package com.grahambartley.nightborn.tools.structures.vampire;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RouteRoomTest {

  private static final Path MOD = Path.of("../../src/main");

  @ParameterizedTest(name = "a {0} block vampire needs {1} on foot, {2} afloat, {3} in all")
  @CsvSource({"1.5, 2, 3, 3", "1.95, 2, 3, 3", "2.0, 3, 3, 3", "2.3, 3, 4, 4", "3.0, 4, 4, 4"})
  void sizesARouteForTheTallerOfWalkingAndFloating(
      final double height, final int onFoot, final int floating, final int headroom) {
    assertEquals(onFoot, RouteRoom.onFoot(height));
    assertEquals(floating, RouteRoom.floating(height));
    assertEquals(headroom, RouteRoom.headroom(height));
  }

  @Test
  void theTallestHeightsAreTheShippedVampireData() {
    assertEquals(TallestVampire.height(), RouteRoom.TALLEST_HOUSEHOLD);
    assertEquals(TallestVampire.height("lord"), RouteRoom.TALLEST_LORD);
  }

  @Test
  void theHoverHeightIsTheOneVampiresFloatAt() throws IOException {
    final Matcher hover =
        Pattern.compile("HOVER_HEIGHT = ([0-9.]+);")
            .matcher(
                Files.readString(
                    MOD.resolve(
                        "java/com/grahambartley/nightborn/levitation/LevitationLift.java")));
    assertEquals(true, hover.find());
    assertEquals(Double.parseDouble(hover.group(1)), RouteRoom.HOVER_HEIGHT);
  }

  @Test
  void theLairCarvesItsCavePathToTheHouseholdRoute() throws IOException {
    final JsonObject room =
        JsonParser.parseString(
                Files.readString(
                    MOD.resolve(
                        "resources/data/nightborn/worldgen/structure/underground_lair.json")))
            .getAsJsonObject()
            .getAsJsonObject("cave")
            .getAsJsonObject("room");
    assertEquals(RouteRoom.HOUSEHOLD, room.get("headroom").getAsInt());
    assertEquals(RouteRoom.LANES, room.get("lanes").getAsInt());
  }
}
