package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RevealPayload(List<Integer> entityIds, List<Integer> colours, int ticks)
    implements CustomPayload {

  public static final CustomPayload.Id<RevealPayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "reveal"));

  public static final PacketCodec<ByteBuf, RevealPayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_INT.collect(PacketCodecs.toList()),
          RevealPayload::entityIds,
          PacketCodecs.INTEGER.collect(PacketCodecs.toList()),
          RevealPayload::colours,
          PacketCodecs.VAR_INT,
          RevealPayload::ticks,
          RevealPayload::new);

  public RevealPayload {
    entityIds = List.copyOf(entityIds);
    colours = List.copyOf(colours);
  }

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
