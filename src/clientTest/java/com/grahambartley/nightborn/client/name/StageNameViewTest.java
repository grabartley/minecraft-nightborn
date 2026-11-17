package com.grahambartley.nightborn.client.name;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
}
