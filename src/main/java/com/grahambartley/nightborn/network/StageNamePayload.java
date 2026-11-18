package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record StageNamePayload(int entityId, Optional<Identifier> stage, Optional<Identifier> rank)
    implements CustomPayload {

  public static final CustomPayload.Id<StageNamePayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "stage_name"));

  public static final PacketCodec<ByteBuf, StageNamePayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_INT,
          StageNamePayload::entityId,
          PacketCodecs.optional(Identifier.PACKET_CODEC),
          StageNamePayload::stage,
          PacketCodecs.optional(Identifier.PACKET_CODEC),
          StageNamePayload::rank,
          StageNamePayload::new);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
