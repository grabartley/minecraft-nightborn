package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.Map;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PowerCooldownsTest {

  static final Identifier RUSH = Identifier.of("nightborn", "core/blood_rush");
  static final Identifier OTHER = Identifier.of("nightborn", "other");

  @ParameterizedTest(name = "started at 100 for {0}, at tick {1}: {2} left")
  @CsvSource({"600, 100, 600", "600, 400, 300", "600, 700, 0", "600, 900, 0", "0, 100, 0"})
  @DisplayName("a cooldown counts down from when the power was used")
  void countsDown(final int cooldown, final long now, final long left) {
    assertEquals(left, PowerCooldowns.NONE.start(RUSH, 100, cooldown).ticksLeft(RUSH, now));
  }

  @Test
  @DisplayName("a power never used has no cooldown")
  void unusedIsReady() {
    assertEquals(0, PowerCooldowns.NONE.ticksLeft(RUSH, 5_000));
  }

  @Test
  @DisplayName("starting a cooldown drops the ones that have already run out")
  void dropsExpired() {
    final PowerCooldowns cooldowns = PowerCooldowns.NONE.start(OTHER, 0, 50).start(RUSH, 100, 600);
    assertFalse(cooldowns.readyAt().containsKey(OTHER));
    assertEquals(Map.of(RUSH, 700L), cooldowns.readyAt());
  }

  @ParameterizedTest(name = "{0} of {1} ticks left -> {2}")
  @CsvSource({"600, 600, 1.0", "300, 600, 0.5", "0, 600, 0.0", "900, 600, 1.0", "10, 0, 0.0"})
  @DisplayName("the bar shades the share of the cooldown still to run")
  void fraction(final long left, final int cooldown, final float share) {
    assertEquals(share, PowerCooldowns.fraction(left, cooldown), 0.0001F);
  }

  @ParameterizedTest(name = "{0} ticks at {1}: {2}")
  @CsvSource({
    "600, 1.0, 600",
    "600, 0.9, 540",
    "100, 0.2, 20",
    "5, 0.5, 3",
    "600, -1, 0",
    "0, 0.9, 0"
  })
  @DisplayName("a skill shortens every cooldown by its share")
  void shortened(final int ticks, final double factor, final int shortened) {
    assertEquals(shortened, PowerCooldowns.shortened(ticks, factor));
  }

  @Test
  @DisplayName("cooldowns survive saving and the network unchanged")
  void roundTrips() {
    final PowerCooldowns cooldowns = PowerCooldowns.NONE.start(RUSH, 40, 600);
    assertEquals(
        cooldowns,
        PowerCooldowns.CODEC
            .parse(
                JsonOps.INSTANCE,
                PowerCooldowns.CODEC.encodeStart(JsonOps.INSTANCE, cooldowns).getOrThrow())
            .getOrThrow());
    final var buf = Unpooled.buffer();
    PowerCooldowns.PACKET_CODEC.encode(buf, cooldowns);
    assertEquals(cooldowns, PowerCooldowns.PACKET_CODEC.decode(buf));
  }
}
