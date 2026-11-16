package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.structure.StructureZone;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record StructureZonePayload(Optional<StructureZone> zone) implements CustomPayload {

  public static final CustomPayload.Id<StructureZonePayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "structure_zone"));

  public static final PacketCodec<ByteBuf, StructureZonePayload> CODEC =
      PacketCodecs.optional(StructureZone.PACKET_CODEC)
          .xmap(StructureZonePayload::new, StructureZonePayload::zone);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
