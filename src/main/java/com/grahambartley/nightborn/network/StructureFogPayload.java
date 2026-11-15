package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.structure.FogZone;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record StructureFogPayload(Optional<FogZone> zone) implements CustomPayload {

  public static final CustomPayload.Id<StructureFogPayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "structure_fog"));

  public static final PacketCodec<ByteBuf, StructureFogPayload> CODEC =
      PacketCodecs.optional(FogZone.PACKET_CODEC)
          .xmap(StructureFogPayload::new, StructureFogPayload::zone);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
