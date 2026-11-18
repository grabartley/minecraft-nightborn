package com.grahambartley.nightborn.mind;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CommandedTest {

  @ParameterizedTest(name = "until {0}, now {1}: over {2}")
  @CsvSource({"600, 599, false", "600, 600, true"})
  @DisplayName("a command ends at its time")
  void over(final long until, final long now, final boolean over) {
    assertEquals(
        over,
        new Commanded(
                UUID.randomUUID(), CommandOrder.FOLLOW, Optional.empty(), Optional.empty(), until)
            .over(now));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"ATTACK", "OPEN_DOOR", "FOLLOW"})
  @DisplayName(
      "a finished order falls back to following, keeping its master and time, and saves whole")
  void follows(final CommandOrder order) {
    final UUID master = UUID.randomUUID();
    final Commanded commanded =
        new Commanded(
            master,
            order,
            Optional.of(UUID.randomUUID()),
            Optional.of(new BlockPos(1, 2, 3)),
            600L);
    assertEquals(
        new Commanded(master, CommandOrder.FOLLOW, Optional.empty(), Optional.empty(), 600L),
        commanded.following());
    assertEquals(
        commanded,
        Commanded.CODEC
            .parse(
                JsonOps.INSTANCE,
                Commanded.CODEC.encodeStart(JsonOps.INSTANCE, commanded).getOrThrow())
            .getOrThrow());
  }
}
