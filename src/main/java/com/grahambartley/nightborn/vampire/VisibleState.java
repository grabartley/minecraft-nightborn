package com.grahambartley.nightborn.vampire;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record VisibleState(boolean eyesGlowing, boolean downed, int weakened) {

  public static final int WEAKENED_CAP = 100;

  public static final VisibleState NONE = new VisibleState(false, false, 0);

  public static final Codec<VisibleState> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.BOOL.fieldOf("eyesGlowing").forGetter(VisibleState::eyesGlowing),
                      Codec.BOOL.fieldOf("downed").forGetter(VisibleState::downed),
                      Codec.intRange(0, WEAKENED_CAP)
                          .fieldOf("weakened")
                          .forGetter(VisibleState::weakened))
                  .apply(instance, VisibleState::new));

  public static final PacketCodec<ByteBuf, VisibleState> PACKET_CODEC =
      PacketCodec.tuple(
          PacketCodecs.BOOL,
          VisibleState::eyesGlowing,
          PacketCodecs.BOOL,
          VisibleState::downed,
          PacketCodecs.VAR_INT,
          VisibleState::weakened,
          VisibleState::new);

  public VisibleState {
    weakened = Math.max(0, Math.min(WEAKENED_CAP, weakened));
  }

  public boolean isWeakened() {
    return weakened > 0;
  }
}
