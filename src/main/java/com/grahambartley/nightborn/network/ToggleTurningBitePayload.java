package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ToggleTurningBitePayload() implements CustomPayload {

  public static final ToggleTurningBitePayload INSTANCE = new ToggleTurningBitePayload();

  public static final CustomPayload.Id<ToggleTurningBitePayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "toggle_turning_bite"));

  public static final PacketCodec<ByteBuf, ToggleTurningBitePayload> CODEC =
      PacketCodec.unit(INSTANCE);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
