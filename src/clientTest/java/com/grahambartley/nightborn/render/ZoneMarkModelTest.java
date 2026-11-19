package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ZoneMarkModelTest {

  private static final int PILLAR_SIDES_V = 68;
  private static final int PILLAR_SIDES_HEIGHT = 48;
  private static final int PILLAR_SIDES_WIDTH = 112;

  private static final Path GEO = GeoAssets.file(ZoneMarkModel.GEO);
  private static final Path CLIPS = GeoAssets.file(ZoneMarkModel.ANIMATION);
  private static final Path ATTACKS =
      Path.of("src/main/resources/data/nightborn/nightborn/boss_attack");
  private static final Path SKILLS = Path.of("src/main/resources/data/nightborn/nightborn/skill");

  @ParameterizedTest(name = "line {0}")
  @ValueSource(booleans = {false, true})
  @DisplayName("a circle and a line each have their own sheet, at the size the geometry maps")
  void sheetFitsTheGeometry(final boolean line) throws IOException {
    final JsonObject description = GeoAssets.geometry(GEO).getAsJsonObject("description");
    final Path texture = GeoAssets.mainFile(ZoneMarkModel.texture(line, Optional.empty()));
    assertTrue(Files.exists(texture), texture.toString());
    assertEquals(line, texture.getFileName().toString().contains("line"));
    final BufferedImage image = ImageIO.read(texture.toFile());
    assertEquals(description.get("texture_width").getAsInt(), image.getWidth());
    assertEquals(description.get("texture_height").getAsInt(), image.getHeight());
  }

  static Stream<String> marks() throws IOException {
    final List<String> marks;
    try (Stream<Path> files = Files.list(ATTACKS)) {
      marks =
          files
              .map(ZoneMarkModelTest::attack)
              .filter(attack -> attack.has("zone"))
              .map(attack -> attack.getAsJsonObject("zone"))
              .filter(zone -> zone.has("mark"))
              .map(zone -> zone.get("mark").getAsString())
              .collect(Collectors.toCollection(ArrayList::new));
    }
    skillLooks().forEach(look -> marks.add(look.get("look").getAsString()));
    return marks.stream().distinct();
  }

  static Stream<JsonObject> skillLooks() throws IOException {
    try (Stream<Path> files = Files.walk(SKILLS)) {
      return files
          .filter(file -> file.toString().endsWith(".json"))
          .map(ZoneMarkModelTest::attack)
          .filter(skill -> skill.has("power"))
          .flatMap(skill -> skill.getAsJsonObject("power").entrySet().stream())
          .filter(entry -> entry.getValue().isJsonObject())
          .map(entry -> entry.getValue().getAsJsonObject())
          .filter(part -> part.has("look"))
          .toList()
          .stream();
    }
  }

  static Stream<String> pillars() throws IOException {
    return skillLooks()
        .filter(part -> part.has("pillar") && part.get("pillar").getAsBoolean())
        .map(part -> part.get("look").getAsString());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("pillars")
  @DisplayName("every look that raises a pillar has light painted where the pillar draws")
  void pillarsHaveArt(final String mark) throws IOException {
    final BufferedImage image =
        ImageIO.read(
            GeoAssets.mainFile(ZoneMarkModel.texture(false, Optional.of(Identifier.of(mark))))
                .toFile());
    int lit = 0;
    for (int y = PILLAR_SIDES_V; y < PILLAR_SIDES_V + PILLAR_SIDES_HEIGHT; y++) {
      for (int x = 0; x < PILLAR_SIDES_WIDTH; x++) {
        if ((image.getRGB(x, y) >>> 24) > 0) {
          lit++;
        }
      }
    }
    assertTrue(lit > PILLAR_SIDES_WIDTH * PILLAR_SIDES_HEIGHT / 2, mark + " lit " + lit);
  }

  private static JsonObject attack(final Path file) {
    try {
      return GeoAssets.read(file);
    } catch (final IOException unread) {
      throw new UncheckedIOException(unread);
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("marks")
  @DisplayName(
      "every mark an attack or a skill names has its own sheet, at the size the geometry maps")
  void namedMarksFitTheGeometry(final String mark) throws IOException {
    final JsonObject description = GeoAssets.geometry(GEO).getAsJsonObject("description");
    final Path texture =
        GeoAssets.mainFile(ZoneMarkModel.texture(false, Optional.of(Identifier.of(mark))));
    assertTrue(Files.exists(texture), texture.toString());
    final BufferedImage image = ImageIO.read(texture.toFile());
    assertEquals(description.get("texture_width").getAsInt(), image.getWidth());
    assertEquals(description.get("texture_height").getAsInt(), image.getHeight());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"tell", "linger"})
  @DisplayName("the mark has a looping clip for its warning and for lingering")
  void hasItsClips(final String clip) throws IOException {
    final JsonObject shipped = GeoAssets.animations(CLIPS).getAsJsonObject(clip);
    assertTrue(shipped != null, clip + " is missing");
    assertTrue(shipped.get("loop").getAsBoolean());
  }
}
