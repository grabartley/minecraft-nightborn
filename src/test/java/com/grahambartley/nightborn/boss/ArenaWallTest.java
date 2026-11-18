package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ArenaWallTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({"#FFF6DC, 16774876", "FFFFFF, 16777215", "#000000, 0", "#D9C68A, 14272138"})
  @DisplayName("a wall colour is six hex digits, with or without a hash")
  void readsColours(final String text, final int colour) {
    assertEquals(colour, ArenaWall.colour(text).getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"#FFF", "#GGGGGG", "white", "#FFF6DC00"})
  @DisplayName("anything else is refused")
  void refusesBadColours(final String text) {
    assertEquals(true, ArenaWall.colour(text).isError());
  }

  @Test
  @DisplayName("a wall reads its textures, palette and breath from data and writes them back")
  void roundTrips() {
    final ArenaWall wall =
        new ArenaWall(
            Identifier.of("nightborn", "textures/entity/boss/arena_light.png"),
            Identifier.of("nightborn", "textures/entity/boss/arena_light_shapes.png"),
            List.of(0xFFF6DC, 0xD9C68A),
            120,
            true);
    final var json =
        JsonParser.parseString(
            "{\"fog\":\"nightborn:textures/entity/boss/arena_light.png\","
                + "\"shapes\":\"nightborn:textures/entity/boss/arena_light_shapes.png\","
                + "\"palette\":[\"#FFF6DC\",\"#D9C68A\"],\"beat_ticks\":120,\"breathes\":true}");
    assertEquals(wall, ArenaWall.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    assertEquals(json, ArenaWall.CODEC.encodeStart(JsonOps.INSTANCE, wall).getOrThrow());
  }

  @Test
  @DisplayName("a wall with no colours is refused")
  void needsAColour() {
    assertEquals(
        true,
        ArenaWall.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"fog\":\"a:b\",\"shapes\":\"a:c\",\"palette\":[],\"beat_ticks\":40}"))
            .isError());
  }
}
