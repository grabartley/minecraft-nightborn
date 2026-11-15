package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.nightborn.config.DownedSettings;
import com.grahambartley.nightborn.config.FeedingSettings;
import com.grahambartley.nightborn.config.NightbornConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import java.util.stream.Stream;
import net.minecraft.network.codec.PacketCodecs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ConfigPacketCodecTest {

  static Stream<NightbornConfig> configs() {
    return Stream.of(
        NightbornConfig.DEFAULTS,
        NightbornConfig.DEFAULTS
            .with(FeedingSettings.WITNESS_RANGE, 0)
            .with(DownedSettings.HARDCORE_VAMPIRE_DEATH, true));
  }

  @ParameterizedTest
  @MethodSource("configs")
  @DisplayName("settings reach the client exactly as the server holds them")
  void roundTrips(final NightbornConfig config) {
    final ByteBuf buf = Unpooled.buffer();
    ConfigPacketCodec.write(config, buf);
    assertEquals(config, ConfigPacketCodec.read(buf));
  }

  @ParameterizedTest(name = "{0} values")
  @ValueSource(ints = {0, 1, 1000, -1})
  @DisplayName("a packet with the wrong number of values is refused")
  void wrongCountRefused(final int count) {
    final ByteBuf buf = Unpooled.buffer();
    PacketCodecs.VAR_INT.encode(buf, count);
    assertThrows(DecoderException.class, () -> ConfigPacketCodec.read(buf));
  }

  @Test
  @DisplayName("out of range values from a modified client are clamped")
  void clampsHostileValues() {
    final ByteBuf buf = Unpooled.buffer();
    ConfigPacketCodec.write(NightbornConfig.DEFAULTS, buf);
    buf.setByte(1, 0x7F);
    final NightbornConfig read = ConfigPacketCodec.read(buf);
    assertEquals(100, read.turning().biteTurnChance());
  }
}
