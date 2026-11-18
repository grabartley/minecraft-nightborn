package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.house.crest.Crest;
import com.grahambartley.nightborn.name.NameRules;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record NameHousePayload(String name, Crest crest) implements CustomPayload {

  public static final CustomPayload.Id<NameHousePayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "name_house"));

  public static final PacketCodec<ByteBuf, NameHousePayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.string(NameRules.MAX_SENT_LENGTH),
          NameHousePayload::name,
          Crest.PACKET_CODEC,
          NameHousePayload::crest,
          NameHousePayload::new);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
