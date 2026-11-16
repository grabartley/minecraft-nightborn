package com.grahambartley.nightborn.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public record AmbientAdditions(Identifier sound, double tickChance) {

  public static final Codec<AmbientAdditions> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("sound").forGetter(AmbientAdditions::sound),
                      Codec.doubleRange(0.0, 1.0)
                          .fieldOf("tick_chance")
                          .forGetter(AmbientAdditions::tickChance))
                  .apply(instance, AmbientAdditions::new));

  public static final PacketCodec<ByteBuf, AmbientAdditions> PACKET_CODEC =
      PacketCodec.tuple(
          Identifier.PACKET_CODEC,
          AmbientAdditions::sound,
          PacketCodecs.DOUBLE,
          AmbientAdditions::tickChance,
          AmbientAdditions::new);
}
