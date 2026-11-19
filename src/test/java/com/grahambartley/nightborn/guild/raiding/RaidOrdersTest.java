package com.grahambartley.nightborn.guild.raiding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RaidOrdersTest {

  private static final UUID QUARRY = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @ParameterizedTest(name = "until {0}, now {1}: over {2}")
  @CsvSource({"37000, 36999, false", "37000, 37000, true", "37000, 40000, true"})
  void isOverAtTheTimeItRunsUntil(final long until, final long now, final boolean over) {
    assertEquals(over, new RaidOrders(QUARRY, until, Optional.empty()).over(now));
  }

  @ParameterizedTest(name = "place {0}")
  @CsvSource({"true", "false"})
  void survivesASaveAndLoad(final boolean housed) {
    final RaidOrders orders =
        new RaidOrders(
            QUARRY, 37000L, housed ? Optional.of(new BlockPos(4, 64, -9)) : Optional.empty());
    assertEquals(
        orders,
        RaidOrders.CODEC
            .parse(
                JsonOps.INSTANCE,
                RaidOrders.CODEC.encodeStart(JsonOps.INSTANCE, orders).getOrThrow())
            .getOrThrow());
  }
}
