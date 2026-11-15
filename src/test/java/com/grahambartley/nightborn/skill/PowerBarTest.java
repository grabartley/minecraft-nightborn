package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PowerBarTest {

  static Identifier id(final int i) {
    return Identifier.of("nightborn", "skill_" + i);
  }

  @ParameterizedTest(name = "{0} skills bought")
  @ValueSource(ints = {0, 1, 4, 9, 14})
  @DisplayName("the bar holds active skills in the order bought, up to its slot count")
  void slots(final int bought) {
    TreeProgress progress = TreeProgress.EMPTY.withEarned(100);
    for (int i = 0; i < bought; i++) {
      progress = progress.buy(id(i), 1, null);
    }
    final Set<Identifier> active =
        Set.copyOf(
            IntStream.range(0, bought).filter(i -> i % 2 == 0).mapToObj(PowerBarTest::id).toList());
    final List<Identifier> expected =
        IntStream.range(0, bought)
            .filter(i -> i % 2 == 0)
            .mapToObj(PowerBarTest::id)
            .limit(PowerBar.SLOTS)
            .toList();
    assertEquals(expected, PowerBar.slots(progress, active::contains));
  }
}
