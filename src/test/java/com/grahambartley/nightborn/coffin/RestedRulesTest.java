package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.house.House;
import java.util.Optional;
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

  private static final UUID LORD = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID MEMBER = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID STRANGER = UUID.fromString("00000000-0000-0000-0000-000000000003");

  @ParameterizedTest(name = "vampire {0}, coffin {1}, fully asleep {2}, night {3}: rested {4}")
  @CsvSource({
    "true, true, true, true, true",
    "false, true, true, true, false",
    "true, false, true, true, false",
    "true, true, false, true, false",
    "true, true, true, false, false"
  })
  @DisplayName("only a vampire player still asleep in a coffin when dusk comes wakes rested")
  void earned(
      final boolean vampire,
      final boolean coffin,
      final boolean asleep,
      final boolean night,
      final boolean rested) {
    assertEquals(rested, RestedRules.earned(vampire, coffin, asleep, night));
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
