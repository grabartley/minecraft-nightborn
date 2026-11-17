package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.name.NameRules;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record NameServantPayload(int entityId, String name) implements CustomPayload {

  public static final CustomPayload.Id<NameServantPayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "name_servant"));

  public static final PacketCodec<ByteBuf, NameServantPayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_INT,
          NameServantPayload::entityId,
          PacketCodecs.string(NameRules.MAX_SENT_LENGTH),
          NameServantPayload::name,
          NameServantPayload::new);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
