package com.grahambartley.nightborn.hunter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class FightRollTest {

  static final UUID ONE = new UUID(0L, 1L);
  static final UUID TWO = new UUID(0L, 2L);
  static final UUID THREE = new UUID(0L, 3L);

  static Stream<Arguments> fights() {
    return Stream.of(
        Arguments.of("nobody", List.of(), List.of()),
        Arguments.of("one hunter", List.of(ONE), List.of(ONE)),
        Arguments.of("one hunter striking twice", List.of(ONE, ONE), List.of(ONE)),
        Arguments.of(
            "three hunters in turn", List.of(TWO, ONE, TWO, THREE), List.of(TWO, ONE, THREE)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("fights")
  @DisplayName("each fighter is on the roll once, in the order they joined, across a save")
  void rollsEachFighterOnce(final String name, final List<UUID> joins, final List<UUID> roll) {
    final FightRoll fight = new FightRoll();
    joins.forEach(fight::join);
    assertEquals(roll, fight.fighters());
    final NbtCompound nbt = new NbtCompound();
    fight.write(nbt);
    final FightRoll loaded = new FightRoll();
    loaded.join(THREE);
    loaded.read(nbt);
    assertEquals(roll, loaded.fighters());
  }

  @ParameterizedTest(name = "{0} fighters")
  @ValueSource(ints = {1, 64, 65, 200})
  @DisplayName("the roll holds no more than its limit")
  void capped(final int joined) {
    final FightRoll fight = new FightRoll();
    for (int i = 0; i < joined; i++) {
      fight.join(new UUID(7L, i));
    }
    assertEquals(Math.min(joined, FightRoll.MOST), fight.fighters().size());
  }

  static Stream<Arguments> shares() {
    return Stream.of(
        Arguments.of("the finisher fought", Optional.of(ONE), List.of(ONE, TWO), List.of(ONE, TWO)),
        Arguments.of(
            "the finisher only struck the last blow",
            Optional.of(THREE),
            List.of(ONE, TWO),
            List.of(ONE, TWO, THREE)),
        Arguments.of("nobody finished it", Optional.empty(), List.of(ONE), List.of(ONE)),
        Arguments.of("a lone finisher", Optional.of(TWO), List.of(), List.of(TWO)),
        Arguments.of("nobody at all", Optional.empty(), List.of(), List.of()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("shares")
  @DisplayName("everyone who took part shares the reward once, the finisher included")
  void sharers(
      final String name,
      final Optional<UUID> finisher,
      final List<UUID> tookPart,
      final List<UUID> sharers) {
    assertEquals(sharers, FightRoll.sharers(finisher, new ArrayList<>(tookPart)));
  }
}
