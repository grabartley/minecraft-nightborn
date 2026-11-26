package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PowerClipPayload(int entityId, String clip) implements CustomPayload {

  public static final CustomPayload.Id<PowerClipPayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "power_clip"));

  public static final PacketCodec<ByteBuf, PowerClipPayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_INT,
          PowerClipPayload::entityId,
          PacketCodecs.STRING,
          PowerClipPayload::clip,
          PowerClipPayload::new);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
