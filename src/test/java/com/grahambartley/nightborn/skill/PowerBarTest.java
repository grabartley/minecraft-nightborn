package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PowerBarTest {

  static final Predicate<Identifier> EVEN =
      id -> Integer.parseInt(id.getPath().substring(6)) % 2 == 0;

  static Identifier id(final int i) {
    return Identifier.of("nightborn", "skill_" + i);
  }

  static TreeProgress owning(final int bought) {
    TreeProgress progress = TreeProgress.EMPTY.withEarned(100);
    for (int i = 0; i < bought; i++) {
      progress = progress.buy(id(i), 1, null);
    }
    return progress;
  }

  @ParameterizedTest(name = "{0} skills bought")
  @ValueSource(ints = {0, 1, 4, 9, 14})
  @DisplayName("with no layout chosen, the bar holds active skills in the order bought")
  void legacyOrder(final int bought) {
    final List<Identifier> expected =
        IntStream.range(0, bought)
            .filter(i -> i % 2 == 0)
            .mapToObj(PowerBarTest::id)
            .limit(PowerBar.SLOTS)
            .toList();
    assertEquals(PowerLayout.inOrder(expected), PowerBar.layout(owning(bought), EVEN));
  }

  @Test
  @DisplayName("a chosen layout is kept, less any power no longer owned or no longer active")
  void chosenLayout() {
    final TreeProgress progress =
        owning(3).withBar(PowerLayout.EMPTY.place(4, id(0)).place(1, id(1)).place(2, id(8)));
    assertEquals(PowerLayout.EMPTY.place(4, id(0)), PowerBar.layout(progress, EVEN));
  }

  @ParameterizedTest(name = "slot {0}, skill {1}: accepted {2}")
  @CsvSource({
    "0, 2, true",
    "4, 0, true",
    "5, 2, false",
    "-1, 2, false",
    "0, 1, false",
    "0, 12, false",
    "0, -1, true"
  })
  @DisplayName("a slot takes only an owned active skill, and any slot can be cleared")
  void set(final int slot, final int skill, final boolean accepted) {
    final Optional<Identifier> placed = skill < 0 ? Optional.empty() : Optional.of(id(skill));
    assertEquals(accepted, PowerBar.set(owning(5), EVEN, slot, placed).isPresent());
  }

  @Test
  @DisplayName("a player with six active skills can put the sixth on the bar")
  void sixthPower() {
    final TreeProgress progress = owning(12);
    final TreeProgress changed = PowerBar.set(progress, EVEN, 1, Optional.of(id(10))).orElseThrow();
    assertEquals(
        PowerLayout.inOrder(List.of(id(0), id(10), id(4), id(6), id(8))),
        PowerBar.layout(changed, EVEN));
  }

  @Test
  @DisplayName("dragging one slot onto another swaps them, and dragging off clears the slot")
  void swapAndClear() {
    final TreeProgress swapped = PowerBar.set(owning(5), EVEN, 0, Optional.of(id(4))).orElseThrow();
    assertEquals(PowerLayout.inOrder(List.of(id(4), id(2), id(0))), PowerBar.layout(swapped, EVEN));
    final TreeProgress cleared = PowerBar.set(swapped, EVEN, 1, Optional.empty()).orElseThrow();
    assertEquals(Optional.empty(), PowerBar.layout(cleared, EVEN).at(1));
    assertEquals(
        Set.of(id(4), id(0)),
        Set.copyOf(
            PowerBar.layout(cleared, EVEN).slots().stream().flatMap(Optional::stream).toList()));
  }

  @Test
  @DisplayName("a learned power joins a chosen layout, and changes nothing while none is chosen")
  void learned() {
    final TreeProgress unchosen = owning(3);
    assertEquals(unchosen, PowerBar.learned(unchosen, id(2)));
    final TreeProgress chosen = unchosen.withBar(PowerLayout.EMPTY.place(1, id(0)));
    assertEquals(
        PowerLayout.EMPTY.place(1, id(0)).place(0, id(2)),
        PowerBar.learned(chosen, id(2)).bar().orElseThrow());
  }
}
