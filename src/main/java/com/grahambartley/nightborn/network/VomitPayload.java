package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record VomitPayload(int entityId) implements CustomPayload {

  public static final CustomPayload.Id<VomitPayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "vomit"));

  public static final PacketCodec<ByteBuf, VomitPayload> CODEC =
      PacketCodecs.VAR_INT.xmap(VomitPayload::new, VomitPayload::entityId);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
