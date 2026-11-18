package com.grahambartley.nightborn.structure;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

public record StructureZone(Identifier structure, FogZone area, StructureAmbience ambience) {

  public static final PacketCodec<ByteBuf, StructureZone> PACKET_CODEC =
      PacketCodec.tuple(
          Identifier.PACKET_CODEC,
          StructureZone::structure,
          FogZone.PACKET_CODEC,
          StructureZone::area,
          StructureAmbience.PACKET_CODEC,
          StructureZone::ambience,
          StructureZone::new);
}
