package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PowerLayoutTest {

  static Optional<Identifier> slot(final String letter) {
    return letter.equals("-") ? Optional.empty() : Optional.of(Identifier.of("nightborn", letter));
  }

  static PowerLayout layout(final String letters) {
    return new PowerLayout(Arrays.stream(letters.split("")).map(PowerLayoutTest::slot).toList());
  }

  @ParameterizedTest(name = "{0} place {2} on {1} -> {3}")
  @CsvSource({
    "-----, 0, a, a----",
    "a----, 3, b, a--b-",
    "ab---, 1, a, ba---",
    "ab---, 0, b, ba---",
    "abcde, 4, a, ebcda",
    "abcde, 2, f, abfde",
    "ab---, 5, c, ab---",
    "ab---, -1, c, ab---"
  })
  @DisplayName("placing a power fills the slot, and a power already on the bar swaps places")
  void place(final String before, final int slot, final String skill, final String after) {
    assertEquals(layout(after), layout(before).place(slot, Identifier.of("nightborn", skill)));
  }

  @ParameterizedTest(name = "{0} clear {1} -> {2}")
  @CsvSource({"abc--, 1, a-c--", "abc--, 4, abc--", "abc--, 9, abc--", "abc--, -1, abc--"})
  @DisplayName("clearing a slot empties it and leaves the others where they are")
  void clear(final String before, final int slot, final String after) {
    assertEquals(layout(after), layout(before).clear(slot));
  }

  @ParameterizedTest(name = "{0} add {1} -> {2}")
  @CsvSource({"-----, a, a----", "a-c--, b, abc--", "abcde, f, abcde", "ab---, a, ab---"})
  @DisplayName("a newly learned power takes the first free slot, if there is one")
  void add(final String before, final String skill, final String after) {
    assertEquals(layout(after), layout(before).add(Identifier.of("nightborn", skill)));
  }

  @ParameterizedTest(name = "{0} uses {1} slots")
  @CsvSource({"-----, 0", "a----, 1", "--c--, 3", "a---e, 5"})
  @DisplayName("the bar is drawn as far as its last filled slot")
  void used(final String letters, final int used) {
    assertEquals(used, layout(letters).used());
    assertEquals(used == 0, layout(letters).isEmpty());
  }

  @Test
  @DisplayName("a layout always has five slots and never holds one power twice")
  void normalises() {
    assertEquals(layout("a----"), new PowerLayout(List.of(slot("a"))));
    assertEquals(layout("a-b--"), layout("aab--"));
    assertEquals(layout("abcde"), layout("abcdefg"));
    assertEquals(
        layout("abcde"),
        PowerLayout.inOrder(layout("abcdefg").slots().stream().flatMap(Optional::stream).toList()));
  }

  @Test
  @DisplayName("keeping only some powers empties the slots of the rest")
  void keeping() {
    final Identifier b = Identifier.of("nightborn", "b");
    assertEquals(layout("-b---"), layout("abc--").keeping(b::equals));
  }

  @Test
  @DisplayName("a layout survives a save and load with its gaps")
  void roundTrips() {
    final PowerLayout layout = layout("a-c-e");
    assertEquals(
        layout,
        PowerLayout.CODEC
            .parse(
                JsonOps.INSTANCE,
                PowerLayout.CODEC.encodeStart(JsonOps.INSTANCE, layout).getOrThrow())
            .getOrThrow());
  }
}
