package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.sound.MusicSound;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class StructureMusicTest {

  private static final Identifier GOTHIC = Identifier.of("nightborn", "music.gothic");

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"sound\": \"nightborn:music.gothic\", \"min_delay\": 600, \"max_delay\": 2400} | true",
        "{\"sound\": \"nightborn:music.gothic\", \"min_delay\": 0, \"max_delay\": 0} | true",
        "{\"sound\": \"nightborn:music.gothic\", \"min_delay\": 2400, \"max_delay\": 600} | false",
        "{\"sound\": \"nightborn:music.gothic\", \"min_delay\": -1, \"max_delay\": 600} | false",
        "{\"sound\": \"nightborn:music.gothic\", \"min_delay\": 600} | false",
        "{\"min_delay\": 600, \"max_delay\": 2400} | false"
      })
  @DisplayName("music names a sound and a gap between tracks that runs from min to max")
  void validates(final String json, final boolean loads) {
    assertEquals(
        loads,
        StructureMusic.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "replace_current_music {0} reads as {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "| true",
        ", \"replace_current_music\": true | true",
        ", \"replace_current_music\": false | false"
      })
  @DisplayName("structure music cuts off other music unless the data says otherwise")
  void replacesByDefault(final String field, final boolean replaces) {
    final String json =
        "{\"sound\": \"nightborn:music.gothic\", \"min_delay\": 600, \"max_delay\": 2400"
            + (field == null ? "" : field)
            + "}";
    assertEquals(
        replaces,
        StructureMusic.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .getOrThrow()
            .replaceCurrentMusic());
  }

  @ParameterizedTest(name = "{0} to {1}, replace {2}")
  @CsvSource({"600, 2400, true", "0, 0, false", "12000, 24000, true"})
  @DisplayName("music crosses the network unchanged and becomes the same vanilla music")
  void roundTripsAndConverts(final int min, final int max, final boolean replace) {
    final StructureMusic music = new StructureMusic(GOTHIC, min, max, replace);
    final ByteBuf buf = Unpooled.buffer();
    StructureMusic.PACKET_CODEC.encode(buf, music);
    assertEquals(music, StructureMusic.PACKET_CODEC.decode(buf));
    final MusicSound vanilla = music.toMusic();
    assertEquals(GOTHIC, vanilla.getSound().value().getId());
    assertEquals(min, vanilla.getMinDelay());
    assertEquals(max, vanilla.getMaxDelay());
    assertEquals(replace, vanilla.shouldReplaceCurrentMusic());
  }
}
