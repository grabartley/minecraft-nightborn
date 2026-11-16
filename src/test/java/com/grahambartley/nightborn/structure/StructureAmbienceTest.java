package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class StructureAmbienceTest {

  private static final Identifier LOOP = Identifier.of("nightborn", "ambient.lair.loop");
  private static final AmbientAdditions ADDITIONS =
      new AmbientAdditions(Identifier.of("nightborn", "ambient.lair.additions"), 0.0111);
  private static final StructureMusic MUSIC =
      new StructureMusic(Identifier.of("nightborn", "music.gothic"), 600, 2400, true);

  static Stream<StructureAmbience> ambiences() {
    return Stream.of(
        StructureAmbience.SILENT,
        new StructureAmbience(Optional.of(LOOP), Optional.empty(), Optional.empty()),
        new StructureAmbience(Optional.empty(), Optional.of(ADDITIONS), Optional.empty()),
        new StructureAmbience(Optional.empty(), Optional.empty(), Optional.of(MUSIC)),
        new StructureAmbience(Optional.of(LOOP), Optional.of(ADDITIONS), Optional.of(MUSIC)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("ambiences")
  @DisplayName("any mix of loop, additions and music crosses the network unchanged")
  void roundTrips(final StructureAmbience ambience) {
    final ByteBuf buf = Unpooled.buffer();
    StructureAmbience.PACKET_CODEC.encode(buf, ambience);
    assertEquals(ambience, StructureAmbience.PACKET_CODEC.decode(buf));
  }

  @ParameterizedTest(name = "{0}: loop {1}, additions {2}, music {3}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{} | false | false | false",
        "{\"ambient_sound\": \"nightborn:ambient.lair.loop\"} | true | false | false",
        "{\"additions_sound\": {\"sound\": \"nightborn:ambient.lair.additions\", \"tick_chance\": 0.0111}} | false | true | false",
        "{\"music\": {\"sound\": \"nightborn:music.gothic\", \"min_delay\": 600, \"max_delay\": 2400}} | false | false | true"
      })
  @DisplayName("every part of a structure's ambience is optional")
  void partsAreOptional(
      final String json, final boolean loop, final boolean additions, final boolean music) {
    final StructureAmbience ambience =
        StructureAmbience.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(loop, ambience.loop().isPresent());
    assertEquals(additions, ambience.additions().isPresent());
    assertEquals(music, ambience.music().isPresent());
  }
}
