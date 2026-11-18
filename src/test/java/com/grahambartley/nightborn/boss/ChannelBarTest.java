package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.entity.boss.BossBar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChannelBarTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({"green, GREEN, nightborn.boss.praying", "white, WHITE, nightborn.boss.warded"})
  @DisplayName("a channel's bar names its colour and its words")
  void parses(final String name, final BossBar.Color color, final String key) {
    assertEquals(
        new ChannelBar(color, key),
        ChannelBar.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("{\"color\":\"" + name + "\",\"key\":\"" + key + "\"}"))
            .getOrThrow());
  }
}
