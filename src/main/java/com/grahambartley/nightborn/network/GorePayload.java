package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.gore.Gore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record GorePayload(int entityId, Gore gore, float sprayYaw) implements CustomPayload {

  public static final CustomPayload.Id<GorePayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "gore"));

  public static final PacketCodec<ByteBuf, GorePayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_INT,
          GorePayload::entityId,
          Gore.PACKET_CODEC,
          GorePayload::gore,
          PacketCodecs.FLOAT,
          GorePayload::sprayYaw,
          GorePayload::new);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
