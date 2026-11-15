package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.skill.SkillPurchase;
import com.grahambartley.nightborn.skill.SkillTree;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class SkillThemeTest {

  static final String SPRITES = "/assets/nightborn/textures/gui/sprites/";

  static List<Identifier> sprites(final SkillTheme theme) {
    return Stream.concat(
            Stream.of(
                theme.panel(),
                theme.header(),
                theme.tooltip(),
                theme.point(),
                theme.powerSlot(),
                theme.line(false),
                theme.line(true)),
            Stream.of(SkillPurchase.Verdict.values()).map(theme::frame))
        .distinct()
        .toList();
  }

  static BufferedImage image(final Identifier sprite) throws IOException {
    try (InputStream in =
        SkillThemeTest.class.getResourceAsStream(SPRITES + sprite.getPath() + ".png")) {
      assertNotNull(in, sprite.toString());
      return ImageIO.read(in);
    }
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(SkillTree.class)
  @DisplayName("every sprite a theme names ships in the mod, under its own tree's folder")
  void spritesExist(final SkillTree tree) throws IOException {
    for (final Identifier sprite : sprites(SkillTheme.of(tree))) {
      assertTrue(sprite.getPath().startsWith("skills/" + tree.asString() + "/"), sprite.toString());
      assertNotNull(image(sprite));
    }
  }

  static Stream<Arguments> scaled() {
    return Stream.of(SkillTree.values())
        .map(SkillTheme::of)
        .flatMap(
            theme ->
                Stream.of(
                    Arguments.of(theme.panel(), "nine_slice"),
                    Arguments.of(theme.header(), "nine_slice"),
                    Arguments.of(theme.tooltip(), "nine_slice"),
                    Arguments.of(theme.line(false), "tile"),
                    Arguments.of(theme.line(true), "tile")));
  }

  @ParameterizedTest(name = "{0} is {1}")
  @MethodSource("scaled")
  @DisplayName("every stretched sprite says how to scale, at the size of its own image")
  void scaling(final Identifier sprite, final String type) throws IOException {
    final JsonObject scaling;
    try (InputStream in =
        SkillThemeTest.class.getResourceAsStream(SPRITES + sprite.getPath() + ".png.mcmeta")) {
      assertNotNull(in, sprite + " mcmeta");
      scaling =
          JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
              .getAsJsonObject()
              .getAsJsonObject("gui")
              .getAsJsonObject("scaling");
    }
    final BufferedImage image = image(sprite);
    assertEquals(type, scaling.get("type").getAsString());
    assertEquals(image.getWidth(), scaling.get("width").getAsInt());
    assertEquals(image.getHeight(), scaling.get("height").getAsInt());
    if (scaling.has("border")) {
      assertTrue(scaling.get("border").getAsInt() * 2 < image.getWidth());
    }
  }

  @Test
  @DisplayName("the vampire and hunter panels are different pictures with different title colours")
  void treesLookDifferent() throws IOException {
    final SkillTheme vampire = SkillTheme.of(SkillTree.VAMPIRE);
    final SkillTheme hunter = SkillTheme.of(SkillTree.HUNTER);
    final BufferedImage a = image(vampire.panel());
    final BufferedImage b = image(hunter.panel());
    int differing = 0;
    for (int y = 0; y < a.getHeight(); y++) {
      for (int x = 0; x < a.getWidth(); x++) {
        if (a.getRGB(x, y) != b.getRGB(x, y)) {
          differing++;
        }
      }
    }
    assertTrue(differing > a.getWidth() * a.getHeight() / 2, "differing pixels " + differing);
    assertNotEquals(vampire.title(), hunter.title());
  }

  static Stream<Arguments> verdicts() {
    return Stream.of(
        Arguments.of(SkillPurchase.Verdict.OWNED, "frame_owned"),
        Arguments.of(SkillPurchase.Verdict.ALLOWED, "frame_available"),
        Arguments.of(SkillPurchase.Verdict.NEEDS_SKILL, "frame_locked"),
        Arguments.of(SkillPurchase.Verdict.TOO_FEW_POINTS, "frame_locked"),
        Arguments.of(SkillPurchase.Verdict.BLOODLINE_LOCKED, "frame_locked"));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("verdicts")
  @DisplayName("owned, buyable and locked skills each get their own frame")
  void frames(final SkillPurchase.Verdict verdict, final String frame) {
    for (final SkillTree tree : SkillTree.values()) {
      assertEquals(
          Identifier.of("nightborn", "skills/" + tree.asString() + "/" + frame),
          SkillTheme.of(tree).frame(verdict));
    }
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(SkillTree.class)
  @DisplayName("the three frame states differ in outline shape, not only in colour")
  void framesDifferInShape(final SkillTree tree) throws IOException {
    final SkillTheme theme = SkillTheme.of(tree);
    final List<String> outlines =
        Stream.of(
                SkillPurchase.Verdict.OWNED,
                SkillPurchase.Verdict.ALLOWED,
                SkillPurchase.Verdict.NEEDS_SKILL)
            .map(theme::frame)
            .map(SkillThemeTest::outline)
            .toList();
    assertEquals(3, outlines.stream().distinct().count());
  }

  static String outline(final Identifier sprite) {
    try {
      final BufferedImage image = image(sprite);
      final StringBuilder mask = new StringBuilder();
      for (int y = 0; y < image.getHeight(); y++) {
        for (int x = 0; x < image.getWidth(); x++) {
          mask.append((image.getRGB(x, y) >>> 24) > 0 ? '#' : '.');
        }
      }
      return mask.toString();
    } catch (final IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
