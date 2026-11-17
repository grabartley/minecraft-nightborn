package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.name.NameRules;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OpenNamingPayload(int entityId, String suggestion) implements CustomPayload {

  public static final CustomPayload.Id<OpenNamingPayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "open_naming"));

  public static final PacketCodec<ByteBuf, OpenNamingPayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_INT,
          OpenNamingPayload::entityId,
          PacketCodecs.string(NameRules.MAX_SENT_LENGTH),
          OpenNamingPayload::suggestion,
          OpenNamingPayload::new);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
