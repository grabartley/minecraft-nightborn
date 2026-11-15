package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ToggleLevitationPayload() implements CustomPayload {

  public static final ToggleLevitationPayload INSTANCE = new ToggleLevitationPayload();

  public static final CustomPayload.Id<ToggleLevitationPayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "toggle_levitation"));

  public static final PacketCodec<ByteBuf, ToggleLevitationPayload> CODEC =
      PacketCodec.unit(INSTANCE);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
