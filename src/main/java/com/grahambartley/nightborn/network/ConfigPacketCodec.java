package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.config.BoolSetting;
import com.grahambartley.nightborn.config.IntSetting;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.config.Setting;
import com.grahambartley.nightborn.config.SettingValues;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public final class ConfigPacketCodec {

  public static final PacketCodec<ByteBuf, NightbornConfig> CODEC =
      PacketCodec.of(ConfigPacketCodec::write, ConfigPacketCodec::read);

  private ConfigPacketCodec() {}

  static void write(final NightbornConfig config, final ByteBuf buf) {
    PacketCodecs.VAR_INT.encode(buf, NightbornConfig.SETTINGS.size());
    for (final Setting<?> setting : NightbornConfig.SETTINGS) {
      switch (setting) {
        case IntSetting number -> PacketCodecs.VAR_INT.encode(buf, number.get(config));
        case BoolSetting flag -> PacketCodecs.BOOL.encode(buf, flag.get(config));
      }
    }
  }

  static NightbornConfig read(final ByteBuf buf) {
    final int count = PacketCodecs.VAR_INT.decode(buf);
    if (count != NightbornConfig.SETTINGS.size()) {
      throw new DecoderException(
          "Nightborn settings packet has "
              + count
              + " values, expected "
              + NightbornConfig.SETTINGS.size());
    }
    SettingValues values = SettingValues.EMPTY;
    for (final Setting<?> setting : NightbornConfig.SETTINGS) {
      values =
          switch (setting) {
            case IntSetting number -> values.with(number, PacketCodecs.VAR_INT.decode(buf));
            case BoolSetting flag -> values.with(flag, PacketCodecs.BOOL.decode(buf));
          };
    }
    return NightbornConfig.from(values);
  }
}
