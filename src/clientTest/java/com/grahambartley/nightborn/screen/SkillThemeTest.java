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
                theme.line(false),
                theme.line(true),
                theme.wheelRing(),
                theme.wheelSlot(false, false),
                theme.wheelSlot(false, true),
                theme.wheelSlot(true, false),
                theme.experienceBar(),
                theme.experienceFill()),
            Stream.concat(
                Stream.of(SkillPurchase.Verdict.values()).map(theme::frame),
                Stream.of(PowerSlotLook.values()).map(theme::powerSlot)))
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
                    Arguments.of(theme.experienceBar(), "nine_slice"),
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

  @ParameterizedTest(name = "{0}")
  @EnumSource(SkillTree.class)
  @DisplayName("a problem line stands apart from the title, text and muted colours")
  void problemColourStandsOut(final SkillTree tree) {
    final SkillTheme theme = SkillTheme.of(tree);
    assertEquals(
        4,
        Stream.of(theme.title(), theme.text(), theme.muted(), theme.problem()).distinct().count());
  }

  static Stream<Arguments> verdicts() {
    return Stream.of(
        Arguments.of(SkillPurchase.Verdict.OWNED, "frame_owned"),
        Arguments.of(SkillPurchase.Verdict.ALLOWED, "frame_available"),
        Arguments.of(SkillPurchase.Verdict.NEEDS_SKILL, "frame_locked"),
        Arguments.of(SkillPurchase.Verdict.TOO_FEW_POINTS, "frame_locked"),
        Arguments.of(SkillPurchase.Verdict.LINE_LOCKED, "frame_locked"));
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

  @ParameterizedTest(name = "{0}")
  @EnumSource(SkillTree.class)
  @DisplayName("the wheel's hover glow lets the ring show through and its spokes are solid")
  void ringColours(final SkillTree tree) {
    final SkillTheme theme = SkillTheme.of(tree);
    assertTrue((theme.ringHover() >>> 24) < 0x80 && (theme.ringHover() >>> 24) > 0x20);
    assertEquals(0xFF, theme.ringEdge() >>> 24);
    assertNotEquals(theme.ringHover() & 0xFFFFFF, theme.ringEdge() & 0xFFFFFF);
  }

  static Stream<Arguments> wheelSlots() {
    return Stream.of(
        Arguments.of(false, false, "wheel_slot"),
        Arguments.of(false, true, "wheel_slot_current"),
        Arguments.of(true, false, "wheel_slot_hover"),
        Arguments.of(true, true, "wheel_slot_hover"));
  }

  @ParameterizedTest(name = "hovered {0}, current {1} -> {2}")
  @MethodSource("wheelSlots")
  @DisplayName("a wheel slot lights up under the pointer, and the order in force has its own frame")
  void wheelSlot(final boolean hovered, final boolean current, final String sprite) {
    for (final SkillTree tree : SkillTree.values()) {
      assertEquals(
          Identifier.of("nightborn", "skills/" + tree.asString() + "/" + sprite),
          SkillTheme.of(tree).wheelSlot(hovered, current));
    }
  }

  static Stream<Arguments> powerSlots() {
    return Stream.of(
        Arguments.of(PowerSlotLook.FILLED, "power_slot"),
        Arguments.of(PowerSlotLook.EMPTY, "power_slot_empty"),
        Arguments.of(PowerSlotLook.LIFTED, "power_slot_lifted"),
        Arguments.of(PowerSlotLook.TARGET, "power_slot_drop"));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("powerSlots")
  @DisplayName("each state of a power bar slot has its own frame")
  void powerSlot(final PowerSlotLook look, final String sprite) {
    for (final SkillTree tree : SkillTree.values()) {
      assertEquals(
          Identifier.of("nightborn", "skills/" + tree.asString() + "/" + sprite),
          SkillTheme.of(tree).powerSlot(look));
    }
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(SkillTree.class)
  @DisplayName("the wheel ring is drawn at the size the ring's geometry expects")
  void ringSize(final SkillTree tree) throws IOException {
    final BufferedImage ring = image(SkillTheme.of(tree).wheelRing());
    assertEquals(CommandWheelScreen.RING, ring.getWidth());
    assertEquals(CommandWheelScreen.RING, ring.getHeight());
    assertTrue(CommandWheelScreen.RING / 2 > WheelRing.OUTER);
    final int centre = CommandWheelScreen.RING / 2;
    assertEquals(0, ring.getRGB(centre, centre) >>> 24, "the middle is see-through");
    assertEquals(
        0xFF, ring.getRGB(centre, centre - (WheelRing.INNER + WheelRing.OUTER) / 2) >>> 24);
  }
}
