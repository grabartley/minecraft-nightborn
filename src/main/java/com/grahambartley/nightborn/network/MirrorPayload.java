package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MirrorPayload(int entityId, boolean reflects) implements CustomPayload {

  public static final CustomPayload.Id<MirrorPayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "mirror"));

  public static final PacketCodec<ByteBuf, MirrorPayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_INT,
          MirrorPayload::entityId,
          PacketCodecs.BOOL,
          MirrorPayload::reflects,
          MirrorPayload::new);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
