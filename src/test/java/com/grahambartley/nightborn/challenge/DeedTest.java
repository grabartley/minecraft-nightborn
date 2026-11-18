package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DeedTest {

  static final Identifier FED = Identifier.of("nightborn", "fed");

  @ParameterizedTest(name = "fact {0} reads as number {1}")
  @CsvSource({"3, 3", "-2, -2", "elder,", "'',"})
  void readsNumbersOnlyWhenTheFactIsOne(final String value, final Integer number) {
    final Deed deed = Deed.of(FED).with("rank", value);
    assertEquals(
        number == null ? OptionalInt.empty() : OptionalInt.of(number), deed.number("rank"));
  }

  @ParameterizedTest(name = "{0} times becomes {1}")
  @CsvSource({"1, 1", "5, 5", "0, 0", "-3, 0"})
  void neverCountsBelowNothing(final int times, final int amount) {
    assertEquals(amount, Deed.of(FED).times(times).amount());
  }

  @ParameterizedTest(name = "a fact set as {0} reads {1}")
  @CsvSource({"true, true", "false, false"})
  void writesTruthAsText(final boolean truth, final String text) {
    assertEquals(Optional.of(text), Deed.of(FED).with("seen", truth).fact("seen"));
    assertEquals(Optional.empty(), Deed.of(FED).fact("seen"));
  }
}
