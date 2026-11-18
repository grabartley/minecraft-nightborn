package com.grahambartley.nightborn.house.crest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.DyeColor;

public record Crest(CrestCharge charge, DyeColor field, DyeColor tincture) {

  public static final Crest PLAIN = new Crest(CrestCharge.BAT, DyeColor.RED, DyeColor.BLACK);

  public static final Codec<Crest> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      CrestCharge.CODEC.fieldOf("charge").forGetter(Crest::charge),
                      DyeColor.CODEC.fieldOf("field").forGetter(Crest::field),
                      DyeColor.CODEC.fieldOf("tincture").forGetter(Crest::tincture))
                  .apply(instance, Crest::new));

  public static final PacketCodec<ByteBuf, Crest> PACKET_CODEC =
      PacketCodec.tuple(
          CrestCharge.PACKET_CODEC,
          Crest::charge,
          DyeColor.PACKET_CODEC,
          Crest::field,
          DyeColor.PACKET_CODEC,
          Crest::tincture,
          Crest::new);
}
