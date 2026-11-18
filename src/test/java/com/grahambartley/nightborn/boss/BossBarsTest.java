package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.entity.boss.BossBar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BossBarsTest {

  @ParameterizedTest(name = "warded {0}, draining {1}, praying {2}: {3}")
  @CsvSource({
    "false, false, false, NONE",
    "true, false, true, BLUE",
    "false, true, true, GREEN",
    "false, false, true, YELLOW"
  })
  @DisplayName("a ward shows first, then feeding, then a channel's own bar")
  void state(
      final boolean warded, final boolean draining, final boolean praying, final String colour) {
    final Optional<ChannelBar> channel =
        praying
            ? Optional.of(new ChannelBar(BossBar.Color.YELLOW, "nightborn.boss.praying"))
            : Optional.empty();
    assertEquals(
        colour,
        BossBars.state(warded, draining, channel).map(look -> look.color().name()).orElse("NONE"));
  }
}
