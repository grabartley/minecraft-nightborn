package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.name.NameRules;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record HouseNamePayload(int entityId, Optional<String> house) implements CustomPayload {

  public static final CustomPayload.Id<HouseNamePayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "house_name"));

  public static final PacketCodec<ByteBuf, HouseNamePayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_INT,
          HouseNamePayload::entityId,
          PacketCodecs.optional(PacketCodecs.string(NameRules.MAX_SENT_LENGTH)),
          HouseNamePayload::house,
          HouseNamePayload::new);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
