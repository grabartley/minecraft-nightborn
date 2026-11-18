package com.grahambartley.nightborn.client.name;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.nightborn.rank.RankMark;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class StageNameViewTest {

  static final Identifier ELDER = Identifier.of("nightborn", "elder");

  @Test
  @DisplayName("a stage the server sends is remembered, and a later empty one forgets it")
  void remembersAndForgets() {
    final Map<Integer, Identifier> stages = new HashMap<>();
    StageNameView.remember(stages, 7, Optional.of(ELDER));
    StageNameView.remember(stages, 8, Optional.empty());
    assertEquals(Map.of(7, ELDER), stages);
    StageNameView.remember(stages, 7, Optional.empty());
    assertEquals(Map.of(), stages);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = Formatting.class,
      names = {"GRAY", "WHITE", "GOLD", "RED"})
  @DisplayName("a name takes its stage colour and keeps its words")
  void colours(final Formatting colour) {
    final Text shown =
        StageNameView.coloured(Text.literal("Mina"), Optional.of(TextColor.fromFormatting(colour)));
    assertEquals("Mina", shown.getString());
    assertEquals(TextColor.fromFormatting(colour), shown.getStyle().getColor());
  }

  @Test
  @DisplayName("a name with no stage is drawn as any player's name")
  void plainWithoutAStage() {
    final Text name = Text.literal("Jonathan");
    assertSame(name, StageNameView.coloured(name, Optional.empty()));
  }

  @ParameterizedTest(name = "{0} in {1}")
  @CsvSource({"◇, BLUE", "◆, BLUE", "✦, DARK_AQUA", "✠, AQUA"})
  @DisplayName("a rank mark is drawn before the name in its own colour, leaving the name's alone")
  void marks(final String glyph, final Formatting colour) {
    final Text shown =
        StageNameView.marked(
            Text.literal("Abraham"),
            Optional.of(new RankMark(glyph, TextColor.fromFormatting(colour))));
    assertEquals(glyph + " Abraham", shown.getString());
    assertEquals(
        TextColor.fromFormatting(colour), shown.getSiblings().get(0).getStyle().getColor());
    assertNull(shown.getSiblings().get(2).getStyle().getColor());
  }

  @Test
  @DisplayName("a name with no rank mark is drawn unchanged")
  void plainWithoutAMark() {
    final Text name = Text.literal("Lucy");
    assertSame(name, StageNameView.marked(name, Optional.empty()));
  }

  @ParameterizedTest(name = "{0} with title {1} reads {2}")
  @CsvSource({"Mina, the Undying, Mina the Undying", "Abe, '', Abe"})
  @DisplayName("a worn title follows the name, and none leaves the name alone")
  void titled(final String name, final String title, final String shown) {
    final Optional<Text> worn = Optional.of(title).filter(t -> !t.isEmpty()).map(Text::literal);
    final Text titled = StageNameView.titled(Text.literal(name), worn);
    assertEquals(
        worn.isEmpty() ? name : StageNameView.TITLED,
        worn.isEmpty()
            ? titled.getString()
            : ((TranslatableTextContent) titled.getContent()).getKey());
  }

  @ParameterizedTest(name = "{0} of house {1}")
  @CsvSource({"Mina, Valcourt, true", "Abe, '', false"})
  @DisplayName("a house name follows the name it belongs to, and none leaves the name alone")
  void housed(final String name, final String house, final boolean shown) {
    final Text housed =
        StageNameView.housed(Text.literal(name), Optional.of(house).filter(h -> !h.isEmpty()));
    if (!shown) {
      assertEquals(name, housed.getString());
      return;
    }
    final TranslatableTextContent content = (TranslatableTextContent) housed.getContent();
    assertEquals(StageNameView.HOUSED, content.getKey());
    assertEquals(name, ((Text) content.getArgs()[0]).getString());
    assertEquals(house, content.getArgs()[1]);
  }
}
