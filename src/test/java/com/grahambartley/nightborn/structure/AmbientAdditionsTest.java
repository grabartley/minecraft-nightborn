package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AmbientAdditionsTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"sound\": \"nightborn:ambient.crypt.additions\", \"tick_chance\": 0.0111} | true",
        "{\"sound\": \"nightborn:ambient.crypt.additions\", \"tick_chance\": 0} | true",
        "{\"sound\": \"nightborn:ambient.crypt.additions\", \"tick_chance\": 1} | true",
        "{\"sound\": \"nightborn:ambient.crypt.additions\", \"tick_chance\": 1.5} | false",
        "{\"sound\": \"nightborn:ambient.crypt.additions\", \"tick_chance\": -0.1} | false",
        "{\"sound\": \"nightborn:ambient.crypt.additions\"} | false",
        "{\"tick_chance\": 0.01} | false",
        "{\"sound\": \"Not An Id\", \"tick_chance\": 0.01} | false"
      })
  @DisplayName("additions name a sound and a chance per tick between 0 and 1")
  void validates(final String json, final boolean loads) {
    assertEquals(
        loads,
        AmbientAdditions.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "{0} at {1}")
  @CsvSource({"nightborn:ambient.castle.additions, 0.0111", "minecraft:ambient.cave, 1.0"})
  @DisplayName("additions cross the network unchanged")
  void roundTrips(final String sound, final double chance) {
    final AmbientAdditions additions = new AmbientAdditions(Identifier.of(sound), chance);
    final ByteBuf buf = Unpooled.buffer();
    AmbientAdditions.PACKET_CODEC.encode(buf, additions);
    assertEquals(additions, AmbientAdditions.PACKET_CODEC.decode(buf));
  }
}
