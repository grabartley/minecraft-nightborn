package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.house.crest.Crest;
import com.grahambartley.nightborn.name.NameRules;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OpenHouseNamingPayload(String suggestion, Crest crest) implements CustomPayload {

  public static final CustomPayload.Id<OpenHouseNamingPayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "open_house_naming"));

  public static final PacketCodec<ByteBuf, OpenHouseNamingPayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.string(NameRules.MAX_SENT_LENGTH),
          OpenHouseNamingPayload::suggestion,
          Crest.PACKET_CODEC,
          OpenHouseNamingPayload::crest,
          OpenHouseNamingPayload::new);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
