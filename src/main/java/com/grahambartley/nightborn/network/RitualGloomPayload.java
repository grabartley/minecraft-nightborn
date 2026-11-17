package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record RitualGloomPayload(BlockPos altar, int buildUpTicks, int range)
    implements CustomPayload {

  public static final CustomPayload.Id<RitualGloomPayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "ritual_gloom"));

  public static final PacketCodec<ByteBuf, RitualGloomPayload> CODEC =
      PacketCodec.tuple(
          BlockPos.PACKET_CODEC,
          RitualGloomPayload::altar,
          PacketCodecs.VAR_INT,
          RitualGloomPayload::buildUpTicks,
          PacketCodecs.VAR_INT,
          RitualGloomPayload::range,
          RitualGloomPayload::new);

  public static RitualGloomPayload lifted(final BlockPos altar) {
    return new RitualGloomPayload(altar, 0, 0);
  }

  public boolean lifts() {
    return range <= 0;
  }

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
