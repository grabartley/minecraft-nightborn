package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ChooseWheelPayload(int entityId, Identifier action, int argument)
    implements CustomPayload {

  public static final CustomPayload.Id<ChooseWheelPayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "choose_wheel"));

  public static final PacketCodec<ByteBuf, ChooseWheelPayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_INT,
          ChooseWheelPayload::entityId,
          Identifier.PACKET_CODEC,
          ChooseWheelPayload::action,
          PacketCodecs.VAR_INT,
          ChooseWheelPayload::argument,
          ChooseWheelPayload::new);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
