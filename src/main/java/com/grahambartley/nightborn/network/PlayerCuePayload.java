package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PlayerCuePayload(int entityId, PlayerCue cue) implements CustomPayload {

  public static final CustomPayload.Id<PlayerCuePayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "player_cue"));

  public static final PacketCodec<ByteBuf, PlayerCuePayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_INT,
          PlayerCuePayload::entityId,
          PlayerCue.PACKET_CODEC,
          PlayerCuePayload::cue,
          PlayerCuePayload::new);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
