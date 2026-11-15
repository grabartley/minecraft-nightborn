package com.grahambartley.nightborn.faction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class FactionTest {

  static Stream<Arguments> savedNames() {
    return Stream.of(
        Arguments.of(Faction.HUMAN, "human"), Arguments.of(Faction.VAMPIRE, "vampire"));
  }

  @ParameterizedTest(name = "{0} saves as \"{1}\"")
  @MethodSource("savedNames")
  @DisplayName("each faction saves under a fixed name so old worlds keep loading")
  void savesUnderAFixedName(final Faction faction, final String name) {
    assertEquals(
        new JsonPrimitive(name), Faction.CODEC.encodeStart(JsonOps.INSTANCE, faction).getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Faction.class)
  @DisplayName("each faction loads back as itself")
  void loadsBackAsItself(final Faction faction) {
    final var saved = Faction.CODEC.encodeStart(JsonOps.INSTANCE, faction).getOrThrow();
    assertEquals(faction, Faction.CODEC.parse(JsonOps.INSTANCE, saved).getOrThrow());
  }

  @ParameterizedTest(name = "\"{0}\"")
  @ValueSource(strings = {"", "Vampire", "hunter", "werewolf"})
  @DisplayName("an unknown saved name is an error, never a guessed faction")
  void unknownSavedNameIsAnError(final String name) {
    assertTrue(Faction.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(name)).isError());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Faction.class)
  @DisplayName("each faction crosses the network as itself")
  void crossesTheNetworkAsItself(final Faction faction) {
    final ByteBuf buffer = Unpooled.buffer();
    Faction.PACKET_CODEC.encode(buffer, faction);
    assertEquals(faction, Faction.PACKET_CODEC.decode(buffer));
    assertEquals(0, buffer.readableBytes());
  }

  @Test
  @DisplayName("every player starts human")
  void everyPlayerStartsHuman() {
    assertEquals(Faction.HUMAN, Faction.DEFAULT);
  }
}
