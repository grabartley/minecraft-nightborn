package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ChooseTitlePayload(Optional<Identifier> title) implements CustomPayload {

  public static final CustomPayload.Id<ChooseTitlePayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "choose_title"));

  public static final PacketCodec<ByteBuf, ChooseTitlePayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.optional(Identifier.PACKET_CODEC),
          ChooseTitlePayload::title,
          ChooseTitlePayload::new);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
