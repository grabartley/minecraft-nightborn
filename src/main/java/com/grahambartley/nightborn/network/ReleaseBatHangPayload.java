package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ReleaseBatHangPayload() implements CustomPayload {

  public static final ReleaseBatHangPayload INSTANCE = new ReleaseBatHangPayload();

  public static final CustomPayload.Id<ReleaseBatHangPayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "release_bat_hang"));

  public static final PacketCodec<ByteBuf, ReleaseBatHangPayload> CODEC =
      PacketCodec.unit(INSTANCE);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
