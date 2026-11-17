package com.grahambartley.nightborn.name;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NameDisplayTest {

  @ParameterizedTest(name = "female={0}")
  @CsvSource({"false, epithet.nightborn.pale.male", "true, epithet.nightborn.pale.female"})
  @DisplayName("an epithet is shown through its translation key, with English as the fallback")
  void translatesEpithet(final boolean female, final String key) {
    final Text shown =
        NameDisplay.shown(Text.literal("Aurelian the Pale"), "the Pale", female, Optional.empty());
    assertEquals("Aurelian the Pale", shown.getString());
    final TranslatableTextContent pattern = (TranslatableTextContent) shown.getContent();
    assertEquals(NameDisplay.WITH_EPITHET, pattern.getKey());
    assertEquals("Aurelian", ((Text) pattern.getArgs()[0]).getString());
    final TranslatableTextContent epithet =
        (TranslatableTextContent) ((Text) pattern.getArgs()[1]).getContent();
    assertEquals(key, epithet.getKey());
    assertEquals("the Pale", epithet.getFallback());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"Old Wren", "Aurelian the Paler"})
  @DisplayName("a renamed mob shows exactly the name it was given")
  void renamedShowsAsIs(final String name) {
    final Text shown = NameDisplay.shown(Text.literal(name), "the Pale", false, Optional.empty());
    assertEquals(name, shown.getString());
    assertEquals(0, shown.getSiblings().size());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "Aurelian the Pale, the Pale, GOLD",
    "Old Wren, the Pale, DARK_RED",
    "Tobias, '', BLUE"
  })
  @DisplayName("the colour comes from the bearer, so a renamed mob keeps it")
  void colours(final String name, final String epithet, final Formatting colour) {
    assertEquals(
        TextColor.fromFormatting(colour),
        NameDisplay.shown(
                Text.literal(name), epithet, false, Optional.of(TextColor.fromFormatting(colour)))
            .getStyle()
            .getColor());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"Hester", "Bartholomew"})
  @DisplayName("a name with no colour keeps the normal name colour")
  void uncoloured(final String name) {
    assertEquals(
        null,
        NameDisplay.shown(Text.literal(name), "", false, Optional.empty()).getStyle().getColor());
  }

  @ParameterizedTest(name = "own={0} style={1}")
  @CsvSource({"GOLD, BLUE, GOLD", ", BLUE, BLUE", "GOLD, , GOLD"})
  @DisplayName("a bearer's own colour wins over its style's colour")
  void bearerColourFirst(final Formatting own, final Formatting style, final Formatting shown) {
    final NameBearer bearer =
        new NameBearer() {
          @Override
          public boolean bearsWomansName() {
            return false;
          }

          @Override
          public Optional<TextColor> nameColor() {
            return Optional.ofNullable(own).map(TextColor::fromFormatting);
          }
        };
    assertEquals(
        TextColor.fromFormatting(shown),
        NameDisplay.shown(
                Text.literal("Hester"),
                bearer,
                Optional.ofNullable(style).map(TextColor::fromFormatting))
            .getStyle()
            .getColor());
  }
}
