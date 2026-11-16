package com.grahambartley.nightborn.structure;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;

public record StructureZone(FogZone area, StructureAmbience ambience) {

  public static final PacketCodec<ByteBuf, StructureZone> PACKET_CODEC =
      PacketCodec.tuple(
          FogZone.PACKET_CODEC,
          StructureZone::area,
          StructureAmbience.PACKET_CODEC,
          StructureZone::ambience,
          StructureZone::new);
}
