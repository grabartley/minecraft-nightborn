package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.age.AgeOutlook;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.text.Text;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class AgeLineTest {

  static final Text FLEDGLING = Text.literal("Fledgling");
  static final Text ADULT = Text.literal("Adult");
  static final Text ELDER = Text.literal("Elder");
  static final Text DRAIN = Text.literal("Drain a villager");

  static Optional<AgeLine.Ahead> ahead(
      final int nightsLeft, final int bloodLeft, final boolean deedDone, final Text deed) {
    return Optional.of(
        new AgeLine.Ahead(
            new AgeOutlook(1, nightsLeft, bloodLeft, deedDone), ADULT, Optional.ofNullable(deed)));
  }

  static Stream<Arguments> lines() {
    return Stream.of(
        Arguments.of(
            "a new fledgling",
            Optional.of(FLEDGLING),
            ahead(10, 100, false, DRAIN),
            List.of(
                Text.translatable("nightborn.age.stage", FLEDGLING),
                Text.translatable("nightborn.age.waiting", 10, 100, ADULT),
                Text.translatable("nightborn.age.deed", DRAIN))),
        Arguments.of(
            "a fledgling who did the deed early",
            Optional.of(FLEDGLING),
            ahead(6, 63, true, DRAIN),
            List.of(
                Text.translatable("nightborn.age.stage", FLEDGLING),
                Text.translatable("nightborn.age.waiting", 6, 63, ADULT),
                Text.translatable("nightborn.age.deed.done", DRAIN))),
        Arguments.of(
            "a next stage that is open",
            Optional.of(FLEDGLING),
            ahead(0, 0, false, DRAIN),
            List.of(
                Text.translatable("nightborn.age.stage", FLEDGLING),
                Text.translatable("nightborn.age.open", ADULT),
                Text.translatable("nightborn.age.deed", DRAIN))),
        Arguments.of(
            "a next stage with no deed",
            Optional.of(FLEDGLING),
            ahead(3, 0, false, null),
            List.of(
                Text.translatable("nightborn.age.stage", FLEDGLING),
                Text.translatable("nightborn.age.waiting", 3, 0, ADULT))),
        Arguments.of(
            "the oldest stage",
            Optional.of(ELDER),
            Optional.empty(),
            List.of(
                Text.translatable("nightborn.age.stage", ELDER),
                Text.translatable("nightborn.age.oldest"))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("lines")
  @DisplayName("the skill screen names the stage, what the next still needs, and its deed")
  void lines(
      final String name,
      final Optional<Text> stage,
      final Optional<AgeLine.Ahead> ahead,
      final List<Text> lines) {
    assertEquals(lines, AgeLine.lines(stage, ahead));
  }

  @ParameterizedTest(name = "panel ends at {0} on a {1} tall screen with {2} lines -> {3}")
  @CsvSource({"200, 480, 3, 206", "470, 480, 3, 443", "480, 480, 2, 454", "10, 20, 3, 0"})
  @DisplayName("the lines sit under the panel and stay on screen")
  void top(final int panelBottom, final int screenHeight, final int lines, final int top) {
    assertEquals(top, AgeLine.top(panelBottom, screenHeight, lines, 9));
  }
}
