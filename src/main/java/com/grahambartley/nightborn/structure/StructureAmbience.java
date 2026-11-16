package com.grahambartley.nightborn.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public record StructureAmbience(
    Optional<Identifier> loop,
    Optional<AmbientAdditions> additions,
    Optional<StructureMusic> music) {

  public static final StructureAmbience SILENT =
      new StructureAmbience(Optional.empty(), Optional.empty(), Optional.empty());

  public static final Codec<StructureAmbience> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC
                          .optionalFieldOf("ambient_sound")
                          .forGetter(StructureAmbience::loop),
                      AmbientAdditions.CODEC
                          .optionalFieldOf("additions_sound")
                          .forGetter(StructureAmbience::additions),
                      StructureMusic.CODEC
                          .optionalFieldOf("music")
                          .forGetter(StructureAmbience::music))
                  .apply(instance, StructureAmbience::new));

  public static final PacketCodec<ByteBuf, StructureAmbience> PACKET_CODEC =
      PacketCodec.tuple(
          PacketCodecs.optional(Identifier.PACKET_CODEC),
          StructureAmbience::loop,
          PacketCodecs.optional(AmbientAdditions.PACKET_CODEC),
          StructureAmbience::additions,
          PacketCodecs.optional(StructureMusic.PACKET_CODEC),
          StructureAmbience::music,
          StructureAmbience::new);
}
