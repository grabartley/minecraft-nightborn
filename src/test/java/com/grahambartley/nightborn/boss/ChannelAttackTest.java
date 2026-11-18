package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChannelAttackTest {

  static final ChannelAttack PRAYER =
      new ChannelAttack(160, 10, 3.0F, 24.0F, Optional.empty(), Optional.empty());
  static final ChannelAttack HOLD =
      new ChannelAttack(100, 10, 0.0F, 0.0F, Optional.empty(), Optional.empty());

  @ParameterizedTest(name = "prayer at tick {0} after {1} damage: {2}")
  @CsvSource({
    "0, 0, HOLD",
    "5, 0, HOLD",
    "10, 0, MEND",
    "150, 23.9, MEND",
    "155, 0, HOLD",
    "40, 24, BROKEN",
    "160, 0, DONE",
    "200, 30, BROKEN"
  })
  @DisplayName("a prayer mends now and then until it ends or enough blows break it")
  void prays(final int tick, final float taken, final ChannelAttack.Step step) {
    assertEquals(step, PRAYER.step(tick, taken));
  }

  @ParameterizedTest(name = "holding at tick {0} after {1} damage: {2}")
  @CsvSource({"0, 0, HOLD", "10, 0, HOLD", "50, 500, HOLD", "100, 0, DONE"})
  @DisplayName("a channel that neither heals nor breaks just holds for its time")
  void holds(final int tick, final float taken, final ChannelAttack.Step step) {
    assertEquals(step, HOLD.step(tick, taken));
  }

  @Test
  @DisplayName("a channel reads its time, healing, breaking, held item and bar from data")
  void parses() {
    assertEquals(
        new ChannelAttack(
            160,
            10,
            3.0F,
            24.0F,
            Optional.of(Identifier.of("nightborn", "gold_crucifix")),
            Optional.of(new ChannelBar(BossBar.Color.GREEN, "nightborn.boss.praying"))),
        ChannelAttack.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"ticks\":160,\"heal\":3,\"break_damage\":24,"
                        + "\"held\":\"nightborn:gold_crucifix\","
                        + "\"bar\":{\"color\":\"green\",\"key\":\"nightborn.boss.praying\"}}"))
            .getOrThrow());
  }
}
