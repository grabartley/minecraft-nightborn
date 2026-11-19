package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.grahambartley.nightborn.guild.party.HunterTentBlock;
import com.grahambartley.nightborn.guild.party.TentClips;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class HunterTentRendererTest {

  private static final Path GEO = GeoAssets.file(FurnitureArt.geo(HunterTentRenderer.ART));
  private static final Path CLIPS = GeoAssets.file(FurnitureArt.animation(HunterTentRenderer.ART));
  private static final Identifier SHEET =
      FurnitureArt.texture(Identifier.of("nightborn", HunterTentRenderer.ART));

  private static JsonObject clip(final String name) throws IOException {
    final JsonObject clip = GeoAssets.animations(CLIPS).getAsJsonObject(name);
    assertNotNull(clip, name);
    return clip;
  }

  private static double[] value(final JsonElement key) {
    final JsonArray array =
        key.isJsonArray() ? key.getAsJsonArray() : key.getAsJsonObject().getAsJsonArray("post");
    return new double[] {
      array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble()
    };
  }

  private static double[] frame(final JsonObject channel, final boolean last) {
    final TreeMap<Double, JsonElement> keys = new TreeMap<>();
    for (final Map.Entry<String, JsonElement> entry : channel.entrySet()) {
      keys.put(Double.parseDouble(entry.getKey()), entry.getValue());
    }
    return value(last ? keys.lastEntry().getValue() : keys.firstEntry().getValue());
  }

  private static void assertRest(final String channelName, final double[] value) {
    final double rest = "scale".equals(channelName) ? 1.0 : 0.0;
    for (final double axis : value) {
      assertEquals(rest, axis, 1e-6, channelName);
    }
  }

  @Test
  @DisplayName("the canvas sheet ships at the size the geometry maps")
  void sheetFitsTheGeometry() throws IOException {
    final JsonObject description = GeoAssets.geometry(GEO).getAsJsonObject("description");
    final BufferedImage image = ImageIO.read(GeoAssets.mainFile(SHEET).toFile());
    assertEquals(description.get("texture_width").getAsInt(), image.getWidth());
    assertEquals(description.get("texture_height").getAsInt(), image.getHeight());
  }

  static Stream<Arguments> clips() {
    return Stream.of(
        Arguments.of(TentClips.IDLE, "true"),
        Arguments.of(TentClips.PITCH, "once"),
        Arguments.of(TentClips.STRIKE, "hold_on_last_frame"));
  }

  @ParameterizedTest(name = "{0} loops {1}")
  @MethodSource("clips")
  @DisplayName("every clip exists with its loop and only moves bones the tent has")
  void clipsMoveRealBones(final String name, final String loop) throws IOException {
    final JsonObject clip = clip(name);
    final String actual = clip.has("loop") ? clip.get("loop").getAsString() : "once";
    assertEquals(loop, actual);
    assertTrue(GeoAssets.boneNames(GEO).containsAll(clip.getAsJsonObject("bones").keySet()));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {TentClips.PITCH, TentClips.STRIKE})
  @DisplayName("pitch ends and strike starts on the standing pose, so idle takes over with no pop")
  void meetsIdleWithoutAPop(final String name) throws IOException {
    final boolean pitch = TentClips.PITCH.equals(name);
    final JsonObject bones = clip(name).getAsJsonObject("bones");
    for (final String bone : bones.keySet()) {
      final JsonObject channels = bones.getAsJsonObject(bone);
      for (final String channel : channels.keySet()) {
        assertRest(channel, frame(channels.getAsJsonObject(channel), pitch));
      }
    }
    final JsonObject idle = clip(TentClips.IDLE).getAsJsonObject("bones");
    for (final String bone : idle.keySet()) {
      final JsonObject channels = idle.getAsJsonObject(bone);
      for (final String channel : channels.keySet()) {
        assertRest(channel, frame(channels.getAsJsonObject(channel), false));
      }
    }
  }

  @Test
  @DisplayName(
      "the code waits out each clip: pitch counts as fresh for its length, strike ends before removal")
  void timingsMatchTheClips() throws IOException {
    final double pitch = clip(TentClips.PITCH).get("animation_length").getAsDouble();
    final double strike = clip(TentClips.STRIKE).get("animation_length").getAsDouble();
    assertEquals(TentClips.PITCH_TICKS, Math.round(pitch * 20));
    assertTrue(
        strike * 20 < HunterTentBlock.STRIKE_TICKS,
        "the strike clip finishes before the tent goes");
  }
}
