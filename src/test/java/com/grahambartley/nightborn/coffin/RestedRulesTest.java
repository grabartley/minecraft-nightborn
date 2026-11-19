package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.house.House;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class RestedRulesTest {

  private static final int WINDOW = 50;

  private static final UUID LORD = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID MEMBER = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID STRANGER = UUID.fromString("00000000-0000-0000-0000-000000000003");

  @ParameterizedTest(
      name = "vampire {0}, coffin {1}, fully asleep {2}, night {3}, whole day {4}: rested {5}")
  @CsvSource({
    "true, true, true, true, true, true",
    "false, true, true, true, true, false",
    "true, false, true, true, true, false",
    "true, true, false, true, true, false",
    "true, true, true, false, true, false",
    "true, true, true, true, false, false"
  })
  @DisplayName("only a vampire player who slept the whole day in a coffin wakes rested at dusk")
  void earned(
      final boolean vampire,
      final boolean coffin,
      final boolean asleep,
      final boolean night,
      final boolean wholeDay,
      final boolean rested) {
    assertEquals(rested, RestedRules.earned(vampire, coffin, asleep, night, wholeDay));
  }

  @ParameterizedTest(name = "skipped {0}, slept from {1}, waking at {2}: whole day {3}")
  @CsvSource({
    "true, -1, 13000, true",
    "true, 12900, 13000, true",
    "false, 23000, 37000, true",
    "false, 24000, 37000, true",
    "false, 24001, 37000, false",
    "false, 36900, 37000, false",
    "false, -1, 37000, false",
    "false, 0, 13000, true",
    "false, 1, 13000, false"
  })
  @DisplayName("a whole day is a skipped one, or sleep from the first hour after dawn until dusk")
  void wholeDay(final boolean skipped, final long from, final long wake, final boolean whole) {
    assertEquals(
        whole,
        RestedRules.wholeDay(
            skipped, from < 0 ? OptionalLong.empty() : OptionalLong.of(from), wake, WINDOW));
  }

  @ParameterizedTest(name = "a {0} s window lets a sleeper lie down by {1}")
  @CsvSource({"0, 23000", "50, 24000", "600, 35000"})
  @DisplayName("the server's window sets how long after dawn a sleeper may lie down")
  void window(final int seconds, final long latest) {
    assertEquals(true, RestedRules.wholeDay(false, OptionalLong.of(latest), 37000, seconds));
    assertEquals(false, RestedRules.wholeDay(false, OptionalLong.of(latest + 1), 37000, seconds));
  }

  @ParameterizedTest(name = "waking at {0}: dawn began at {1}")
  @CsvSource({"13000, -1000", "37000, 23000", "22999, -1000", "23000, 23000", "0, -1000"})
  @DisplayName("the day being slept through began at the last dawn")
  void dawnBefore(final long now, final long dawn) {
    assertEquals(dawn, RestedRules.dawnBefore(now));
  }

  @ParameterizedTest(name = "time {0}: {1} ticks to dawn")
  @CsvSource({"13000, 10000", "37000, 10000", "22999, 1", "23000, 0", "0, 23000", "12999, 10001"})
  @DisplayName("being rested lasts until the next dawn")
  void ticksUntilDawn(final long time, final int ticks) {
    assertEquals(ticks, RestedRules.ticksUntilDawn(time));
  }

  static Stream<Arguments> coffins() {
    final House house =
        House.founded(LORD, GlobalPos.create(World.OVERWORLD, BlockPos.ORIGIN)).withPlayer(MEMBER);
    return Stream.of(
        Arguments.of("the lord's coffin", Optional.of(house), Optional.of(LORD), 1),
        Arguments.of("a member's coffin", Optional.of(house), Optional.of(MEMBER), 1),
        Arguments.of("a stranger's coffin", Optional.of(house), Optional.of(STRANGER), 0),
        Arguments.of("an unclaimed coffin", Optional.of(house), Optional.empty(), 0),
        Arguments.of("no house", Optional.empty(), Optional.of(LORD), 0));
  }

  @ParameterizedTest(name = "{0}: level {3}")
  @MethodSource("coffins")
  @DisplayName("a coffin of the sleeper's own house rests them more")
  void level(
      final String name, final Optional<House> house, final Optional<UUID> owner, final int level) {
    assertEquals(level, RestedRules.level(house, owner));
  }
}
